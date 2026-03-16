package com.loqiu.moneykeeper.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import com.loqiu.moneykeeper.util.RecordTypeNormalizer;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.MoneyKeeperCreateRequest;
import com.loqiu.moneykeeper.vo.MoneyKeeperUpdateRequest;
import com.loqiu.moneykeeper.vo.RecordSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Record Management", description = "CRUD operations for money keeper records")
@RestController
@RequestMapping("/api/records")
public class MoneyKeeperController {

    private static final Logger logger = LogManager.getLogger(MoneyKeeperController.class);

    @Autowired
    private MoneyKeeperService moneyKeeperService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private RecordSearchService recordSearchService;

    @Operation(summary = "Create record")
    @PostMapping
    public ResponseEntity<MoneyKeeper> createRecord(@RequestBody MoneyKeeperCreateRequest createRequest,
                                                    HttpServletRequest request) {
        logger.info("Creating record - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        validateCreateRequest(createRequest);

        Long targetUserId = resolveTargetUserId(request, createRequest.getUserId());
        Category category = requireAccessibleCategory(createRequest.getCategoryId(), targetUserId);
        String normalizedRecordType = normalizeRecordType(createRequest.getType(), true);
        validateRecordType(normalizedRecordType, category.getType());

        MoneyKeeper record = new MoneyKeeper();
        record.setUserId(targetUserId);
        record.setLedgerId(resolveLedgerId(category, targetUserId, null));
        record.setCategoryId(createRequest.getCategoryId());
        record.setType(normalizedRecordType);
        record.setAmount(createRequest.getAmount());
        record.setTransactionDate(createRequest.getTransactionDate());
        record.setNotes(trimToNull(createRequest.getNotes()));

        moneyKeeperService.insertMoneyKeeper(record);
        recordSearchService.syncRecordIfEnabled(record.getId());
        logger.info("Record created successfully - userId: {}, categoryId: {}", targetUserId, createRequest.getCategoryId());
        return ResponseEntity.ok(record);
    }

    @Operation(summary = "Get record by id")
    @GetMapping("/{id}")
    public ResponseEntity<MoneyKeeper> getRecordById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Getting record - recordId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        MoneyKeeper record = requireRecord(id);
        requireSelfOrAdmin(request, record.getUserId(), "You do not have permission to access this record");
        return ResponseEntity.ok(record);
    }

    @Operation(summary = "Get records by user and date range")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MoneyKeeper>> getRecordsByUserIdAndDateRange(@PathVariable Long userId,
                                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                            HttpServletRequest request) {
        logger.info("Getting records by userId and date range - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId, "You do not have permission to access this user's records");
        validateDateRange(startDate, endDate);
        return ResponseEntity.ok(moneyKeeperService.findByUserIdAndDateRange(userId, startDate, endDate));
    }

    @Operation(summary = "Get records by user and type")
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<MoneyKeeper>> getRecordsByUserIdAndType(@PathVariable Long userId,
                                                                       @PathVariable String type,
                                                                       HttpServletRequest request) {
        logger.info("Getting records by userId and type - targetUserId: {}, type: {}, currentUserId: {}", userId, type, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId, "You do not have permission to access this user's records");
        validateRequiredText(type, "Record type is required");
        return ResponseEntity.ok(moneyKeeperService.findByUserIdAndType(userId, normalizeRecordType(type, true)));
    }

    @Operation(summary = "Update record")
    @PutMapping("/{id}")
    public ResponseEntity<MoneyKeeper> updateRecord(@PathVariable Long id,
                                                    @RequestBody MoneyKeeperUpdateRequest updateRequest,
                                                    HttpServletRequest request) {
        logger.info("Updating record - recordId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        if (updateRequest == null) {
            throw new BadRequestException("Request body is required");
        }

        MoneyKeeper existingRecord = requireRecord(id);
        requireSelfOrAdmin(request, existingRecord.getUserId(), "You do not have permission to modify this record");
        validateUpdateRequest(updateRequest);

        Long categoryId = updateRequest.getCategoryId() != null ? updateRequest.getCategoryId() : existingRecord.getCategoryId();
        Category category = requireAccessibleCategory(categoryId, existingRecord.getUserId());
        String recordType = resolveRecordType(updateRequest.getType(), existingRecord.getType());
        validateRecordType(recordType, category.getType());

        MoneyKeeper updatedRecord = new MoneyKeeper();
        updatedRecord.setId(existingRecord.getId());
        updatedRecord.setUserId(existingRecord.getUserId());
        updatedRecord.setLedgerId(resolveLedgerId(category, existingRecord.getUserId(), existingRecord.getLedgerId()));
        updatedRecord.setCategoryId(categoryId);
        updatedRecord.setType(recordType);
        updatedRecord.setAmount(resolveAmount(updateRequest.getAmount(), existingRecord.getAmount()));
        updatedRecord.setTransactionDate(updateRequest.getTransactionDate() != null ? updateRequest.getTransactionDate() : existingRecord.getTransactionDate());
        updatedRecord.setNotes(updateRequest.getNotes() != null ? trimToNull(updateRequest.getNotes()) : existingRecord.getNotes());
        updatedRecord.setCreatedAt(existingRecord.getCreatedAt());
        updatedRecord.setDeletedAt(existingRecord.getDeletedAt());
        updatedRecord.setDeletedTime(existingRecord.getDeletedTime());

        moneyKeeperService.updateById(updatedRecord);
        recordSearchService.syncRecordIfEnabled(existingRecord.getId());
        logger.info("Record updated successfully - recordId: {}", id);
        return ResponseEntity.ok(requireRecord(id));
    }

    @Operation(summary = "Delete record")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Deleting record - recordId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        MoneyKeeper existingRecord = requireRecord(id);
        requireSelfOrAdmin(request, existingRecord.getUserId(), "You do not have permission to delete this record");

        UpdateWrapper<MoneyKeeper> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("deleted_at", 1)
                .set("deleted_time", LocalDateTime.now());
        moneyKeeperService.update(updateWrapper);
        recordSearchService.removeRecordIfEnabled(existingRecord.getId());
        logger.info("Record deleted successfully - recordId: {}", id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all records")
    @GetMapping("/list")
    public ResponseEntity<List<MoneyKeeper>> getAllRecords(HttpServletRequest request) {
        logger.info("Getting all records - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        if (!RequestAuthUtil.isAdmin(request)) {
            queryWrapper.eq("user_id", RequestAuthUtil.requireCurrentUserId(request));
        }
        queryWrapper.orderByDesc("transaction_date", "created_at");
        return ResponseEntity.ok(moneyKeeperService.list(queryWrapper));
    }

    @Operation(summary = "Get records with category name")
    @GetMapping("/listWithCategoryName/{userId}")
    public ResponseEntity<List<MoneyKeeperDTO>> getAllRecordsWithCategoryName(@PathVariable Long userId,
                                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                              HttpServletRequest request) {
        logger.info("Getting records with category names - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId, "You do not have permission to access this user's records");
        validateOptionalDateRange(startDate, endDate);

        List<MoneyKeeperDTO> records = moneyKeeperService.getAllRecordsWithCategoryName(userId, startDate, endDate);
        logger.info("Records with category names found - count: {}", records.size());
        logger.debug("Records details: {}", JSON.toJSONString(records));
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get records by category name")
    @GetMapping("/listByCategoryName/{categoryName}/{userId}")
    public ResponseEntity<List<MoneyKeeperDTO>> getAllRecordsByCategoryName(@PathVariable String categoryName,
                                                                            @PathVariable Long userId,
                                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                            HttpServletRequest request) {
        logger.info("Getting records by category name - targetUserId: {}, categoryName: {}, currentUserId: {}",
                userId, categoryName, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId, "You do not have permission to access this user's records");
        validateRequiredText(categoryName, "Category name is required");
        validateOptionalDateRange(startDate, endDate);

        List<MoneyKeeperDTO> records = moneyKeeperService.getAllRecordsByCategoryName(categoryName.trim(), userId, startDate, endDate);
        logger.info("Records by category names found - count: {}", records.size());
        logger.debug("Records details: {}", JSON.toJSONString(records));
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get records by user")
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<MoneyKeeper>> getRecordsByUserId(@PathVariable Long userId,
                                                                @RequestParam(required = false) String type,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                HttpServletRequest request) {
        logger.info("Getting records by userId - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId, "You do not have permission to access this user's records");
        validateOptionalDateRange(startDate, endDate);

        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (StringUtils.hasText(type)) {
            queryWrapper.eq("type", normalizeRecordType(type, false));
        }
        if (startDate != null && endDate != null) {
            queryWrapper.between("transaction_date", startDate, endDate);
        } else if (startDate != null) {
            queryWrapper.ge("transaction_date", startDate);
        } else if (endDate != null) {
            queryWrapper.le("transaction_date", endDate);
        }
        queryWrapper.orderByDesc("transaction_date", "created_at");
        return ResponseEntity.ok(moneyKeeperService.list(queryWrapper));
    }

    @Operation(summary = "Get summary")
    @GetMapping("/summary/{userId}")
    public ResponseEntity<RecordSummary> getMoneyKeeperSummary(@PathVariable("userId") Long userId,
                                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                               HttpServletRequest request) {
        logger.info("Getting summary - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId, "You do not have permission to access this user's summary");
        validateOptionalDateRange(startDate, endDate);
        return ResponseEntity.ok(moneyKeeperService.getSummary(userId, startDate, endDate));
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

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
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
            throw new BadRequestException("Record type must match the selected category type");
        }
    }

    private Long resolveTargetUserId(HttpServletRequest request, Long requestedUserId) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (RequestAuthUtil.isAdmin(request) && requestedUserId != null) {
            return requestedUserId;
        }
        return currentUserId;
    }


    private Long resolveLedgerId(Category category, Long userId, Long existingLedgerId) {
        if (category != null && category.getLedgerId() != null) {
            return category.getLedgerId();
        }
        if (existingLedgerId != null) {
            return existingLedgerId;
        }
        return ledgerService.getOrCreatePersonalLedger(userId).getId();
    }
    private void requireSelfOrAdmin(HttpServletRequest request, Long userId, String message) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
        if (!RequestAuthUtil.isSelfOrAdmin(request, userId)) {
            throw new ForbiddenException(message);
        }
    }

    private Category requireAccessibleCategory(Long categoryId, Long targetUserId) {
        if (categoryId == null) {
            throw new BadRequestException("Category id is required");
        }
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (!targetUserId.equals(category.getUserId())) {
            throw new BadRequestException("Category does not belong to the target user");
        }
        return category;
    }

    private MoneyKeeper requireRecord(Long recordId) {
        if (recordId == null) {
            throw new BadRequestException("Record id is required");
        }
        MoneyKeeper record = moneyKeeperService.getById(recordId);
        if (record == null) {
            throw new ResourceNotFoundException("Record not found");
        }
        return record;
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
            return RecordTypeNormalizer.normalizeOptional(value, "Record type must be income or expense");
        }
        return RecordTypeNormalizer.normalizeRequired(value, "Record type is required", "Record type must be income or expense");
    }
}

