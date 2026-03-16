package com.loqiu.moneykeeper.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.RecordEventDispatcher;
import com.loqiu.moneykeeper.service.RecordSearchService;
import com.loqiu.moneykeeper.util.RecordTypeNormalizer;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.MoneyKeeperCreateRequest;
import com.loqiu.moneykeeper.vo.MoneyKeeperUpdateRequest;
import com.loqiu.moneykeeper.vo.RecordSummary;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/records")
public class LedgerRecordController {

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private RecordEventDispatcher recordEventDispatcher;

    @GetMapping
    public ResponseEntity<List<MoneyKeeper>> listRecords(@PathVariable Long ledgerId,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false) String type,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                         HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        validateOptionalDateRange(startDate, endDate);

        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", ledgerId)
                .orderByDesc("transaction_date", "created_at");
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        if (StringUtils.hasText(type)) {
            queryWrapper.eq("type", normalizeRecordType(type, false));
        }
        if (startDate != null) {
            queryWrapper.ge("transaction_date", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("transaction_date", endDate);
        }
        return ResponseEntity.ok(moneyKeeperService.list(queryWrapper));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<MoneyKeeper> getRecord(@PathVariable Long ledgerId,
                                                 @PathVariable Long recordId,
                                                 HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        return ResponseEntity.ok(requireLedgerRecord(ledgerId, recordId));
    }

    @PostMapping
    public ResponseEntity<MoneyKeeper> createRecord(@PathVariable Long ledgerId,
                                                    @RequestBody MoneyKeeperCreateRequest createRequest,
                                                    HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        validateCreateRequest(createRequest);

        Long targetUserId = resolveTargetUserId(request, ledgerId, createRequest.getUserId());
        Category category = requireLedgerCategory(ledgerId, createRequest.getCategoryId());
        String normalizedRecordType = normalizeRecordType(createRequest.getType(), true);
        validateRecordType(normalizedRecordType, category.getType());

        MoneyKeeper record = new MoneyKeeper();
        record.setUserId(targetUserId);
        record.setLedgerId(ledgerId);
        record.setCategoryId(category.getId());
        record.setType(normalizedRecordType);
        record.setAmount(createRequest.getAmount());
        record.setTransactionDate(createRequest.getTransactionDate());
        record.setNotes(trimToNull(createRequest.getNotes()));

        moneyKeeperService.insertMoneyKeeper(record);
        recordEventDispatcher.dispatchRecordCreated(ledgerId, record);
        return ResponseEntity.ok(record);
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<MoneyKeeper> updateRecord(@PathVariable Long ledgerId,
                                                    @PathVariable Long recordId,
                                                    @RequestBody MoneyKeeperUpdateRequest updateRequest,
                                                    HttpServletRequest request) {
        if (updateRequest == null) {
            throw new BadRequestException("Request body is required");
        }

        MoneyKeeper existingRecord = requireLedgerRecord(ledgerId, recordId);
        requireRecordEditor(request, ledgerId, existingRecord);
        validateUpdateRequest(updateRequest);

        Long resolvedCategoryId = updateRequest.getCategoryId() != null ? updateRequest.getCategoryId() : existingRecord.getCategoryId();
        Category category = requireLedgerCategory(ledgerId, resolvedCategoryId);
        String resolvedType = resolveRecordType(updateRequest.getType(), existingRecord.getType());
        validateRecordType(resolvedType, category.getType());

        MoneyKeeper updatedRecord = new MoneyKeeper();
        updatedRecord.setId(existingRecord.getId());
        updatedRecord.setUserId(existingRecord.getUserId());
        updatedRecord.setLedgerId(existingRecord.getLedgerId());
        updatedRecord.setCategoryId(category.getId());
        updatedRecord.setType(resolvedType);
        updatedRecord.setAmount(resolveAmount(updateRequest.getAmount(), existingRecord.getAmount()));
        updatedRecord.setTransactionDate(updateRequest.getTransactionDate() != null ? updateRequest.getTransactionDate() : existingRecord.getTransactionDate());
        updatedRecord.setNotes(updateRequest.getNotes() != null ? trimToNull(updateRequest.getNotes()) : existingRecord.getNotes());
        updatedRecord.setCreatedAt(existingRecord.getCreatedAt());
        updatedRecord.setDeletedAt(existingRecord.getDeletedAt());
        updatedRecord.setDeletedTime(existingRecord.getDeletedTime());

        moneyKeeperService.updateById(updatedRecord);
        recordEventDispatcher.dispatchRecordUpdated(ledgerId, existingRecord, updatedRecord);
        return ResponseEntity.ok(requireLedgerRecord(ledgerId, recordId));
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long ledgerId,
                                             @PathVariable Long recordId,
                                             HttpServletRequest request) {
        MoneyKeeper existingRecord = requireLedgerRecord(ledgerId, recordId);
        requireRecordEditor(request, ledgerId, existingRecord);

        UpdateWrapper<MoneyKeeper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", recordId)
                .eq("ledger_id", ledgerId)
                .set("deleted_at", 1)
                .set("deleted_time", LocalDateTime.now());
        moneyKeeperService.update(updateWrapper);
        recordEventDispatcher.dispatchRecordDeleted(ledgerId, existingRecord);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<RecordSummary> getSummary(@PathVariable Long ledgerId,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                    HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        validateOptionalDateRange(startDate, endDate);

        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", ledgerId);
        if (startDate != null) {
            queryWrapper.ge("transaction_date", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("transaction_date", endDate);
        }

        List<MoneyKeeper> records = moneyKeeperService.list(queryWrapper);
        BigDecimal totalIncome = records.stream()
                .filter(record -> "income".equals(record.getType()))
                .map(MoneyKeeper::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = records.stream()
                .filter(record -> "expense".equals(record.getType()))
                .map(MoneyKeeper::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(RecordSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build());
    }

    private void requireLedgerViewer(HttpServletRequest request, Long ledgerId) {
        ledgerService.requireLedger(ledgerId);
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (!ledgerService.hasActiveMembership(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to access this ledger");
        }
    }

    private void requireRecordEditor(HttpServletRequest request, Long ledgerId, MoneyKeeper record) {
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (ledgerService.hasManagementPermission(ledgerId, currentUserId)) {
            return;
        }
        if (!ledgerService.hasActiveMembership(ledgerId, currentUserId) || !currentUserId.equals(record.getUserId())) {
            throw new ForbiddenException("You do not have permission to modify this record");
        }
    }

    private MoneyKeeper requireLedgerRecord(Long ledgerId, Long recordId) {
        if (recordId == null) {
            throw new BadRequestException("Record id is required");
        }
        MoneyKeeper record = moneyKeeperService.getById(recordId);
        if (record == null || !ledgerId.equals(record.getLedgerId())) {
            throw new ResourceNotFoundException("Record not found");
        }
        return record;
    }

    private Category requireLedgerCategory(Long ledgerId, Long categoryId) {
        if (categoryId == null) {
            throw new BadRequestException("Category id is required");
        }
        Category category = categoryService.getById(categoryId);
        if (category == null || !ledgerId.equals(category.getLedgerId())) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private void validateCreateRequest(MoneyKeeperCreateRequest createRequest) {
        if (createRequest == null) {
            throw new BadRequestException("Request body is required");
        }
        if (createRequest.getCategoryId() == null) {
            throw new BadRequestException("Category id is required");
        }
        validateRequiredText(createRequest.getType(), "Record type is required");
        validateAmount(createRequest.getAmount());
        if (createRequest.getTransactionDate() == null) {
            throw new BadRequestException("Transaction date is required");
        }
    }

    private void validateUpdateRequest(MoneyKeeperUpdateRequest updateRequest) {
        if (updateRequest.getType() != null) {
            validateRequiredText(updateRequest.getType(), "Record type cannot be blank");
        }
        if (updateRequest.getAmount() != null) {
            validateAmount(updateRequest.getAmount());
        }
        if (updateRequest.getCategoryId() == null && updateRequest.getType() == null
                && updateRequest.getAmount() == null && updateRequest.getTransactionDate() == null
                && updateRequest.getNotes() == null) {
            throw new BadRequestException("At least one record field must be provided");
        }
    }

    private void validateRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

    private void validateOptionalDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }
    }

    private void validateRecordType(String requestedType, String categoryType) {
        String normalizedRequestedType = normalizeRecordType(requestedType, true);
        String normalizedCategoryType = normalizeRecordType(categoryType, true);
        if (!normalizedRequestedType.equals(normalizedCategoryType)) {
            throw new BadRequestException("Record type must match the selected category type", ErrorKeyConstants.RECORD_TYPE_MISMATCH);
        }
    }

    private Long resolveTargetUserId(HttpServletRequest request, Long ledgerId, Long requestedUserId) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        Long targetUserId = currentUserId;
        if (RequestAuthUtil.isAdmin(request) && requestedUserId != null) {
            targetUserId = requestedUserId;
        }
        if (!ledgerService.hasActiveMembership(ledgerId, targetUserId)) {
            throw new BadRequestException("Target user is not an active member of this ledger");
        }
        return targetUserId;
    }

    private String resolveRecordType(String requestedType, String existingType) {
        String normalizedType = normalizeRecordType(requestedType, false);
        return normalizedType == null ? existingType : normalizedType;
    }

    private BigDecimal resolveAmount(BigDecimal requestedAmount, BigDecimal existingAmount) {
        if (requestedAmount == null) {
            return existingAmount;
        }
        return requestedAmount;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeRecordType(String value, boolean required) {
        if (!required) {
            return RecordTypeNormalizer.normalizeOptional(value, "Record type must be income or expense", ErrorKeyConstants.RECORD_INVALID_TYPE);
        }
        return RecordTypeNormalizer.normalizeRequired(value, "Record type is required", "Record type must be income or expense", ErrorKeyConstants.RECORD_INVALID_TYPE);
    }
}
