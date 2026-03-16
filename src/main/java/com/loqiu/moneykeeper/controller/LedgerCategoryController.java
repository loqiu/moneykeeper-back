package com.loqiu.moneykeeper.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.exception.BadRequestException;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import com.loqiu.moneykeeper.util.RecordTypeNormalizer;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import com.loqiu.moneykeeper.vo.CategoryRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/categories")
public class LedgerCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private RecordSearchService recordSearchService;

    @GetMapping
    public ResponseEntity<List<Category>> listCategories(@PathVariable Long ledgerId,
                                                         @RequestParam(required = false) String type,
                                                         HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ledger_id", ledgerId)
                .orderByDesc("created_at");
        if (StringUtils.hasText(type)) {
            queryWrapper.eq("type", normalizeCategoryType(type, false));
        }
        return ResponseEntity.ok(categoryService.list(queryWrapper));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategory(@PathVariable Long ledgerId,
                                                @PathVariable Long categoryId,
                                                HttpServletRequest request) {
        requireLedgerViewer(request, ledgerId);
        return ResponseEntity.ok(requireLedgerCategory(ledgerId, categoryId));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@PathVariable Long ledgerId,
                                                   @RequestBody CategoryRequest categoryRequest,
                                                   HttpServletRequest request) {
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        requireLedgerManager(request, ledgerId);
        validateCreateRequest(categoryRequest);

        Category category = new Category();
        category.setUserId(currentUserId);
        category.setLedgerId(ledgerId);
        category.setName(categoryRequest.getName().trim());
        category.setIcon(categoryRequest.getIcon().trim());
        category.setColor(categoryRequest.getColor().trim());
        category.setType(normalizeCategoryType(categoryRequest.getType(), true));

        categoryService.insertCategory(category);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long ledgerId,
                                                   @PathVariable Long categoryId,
                                                   @RequestBody CategoryRequest categoryRequest,
                                                   HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        if (categoryRequest == null) {
            throw new BadRequestException("Request body is required");
        }

        Category existingCategory = requireLedgerCategory(ledgerId, categoryId);
        validateUpdateRequest(categoryRequest);

        Category updatedCategory = new Category();
        updatedCategory.setId(existingCategory.getId());
        updatedCategory.setUserId(existingCategory.getUserId());
        updatedCategory.setLedgerId(existingCategory.getLedgerId());
        updatedCategory.setName(resolveString(categoryRequest.getName(), existingCategory.getName()));
        updatedCategory.setIcon(resolveString(categoryRequest.getIcon(), existingCategory.getIcon()));
        updatedCategory.setColor(resolveString(categoryRequest.getColor(), existingCategory.getColor()));
        updatedCategory.setType(resolveCategoryType(categoryRequest.getType(), existingCategory.getType()));
        updatedCategory.setCreatedAt(existingCategory.getCreatedAt());
        updatedCategory.setDeletedAt(existingCategory.getDeletedAt());
        updatedCategory.setDeletedTime(existingCategory.getDeletedTime());

        categoryService.updateById(updatedCategory);
        recordSearchService.refreshCategoryRecordsIfEnabled(existingCategory.getId());
        return ResponseEntity.ok(requireLedgerCategory(ledgerId, categoryId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long ledgerId,
                                               @PathVariable Long categoryId,
                                               HttpServletRequest request) {
        requireLedgerManager(request, ledgerId);
        Category existingCategory = requireLedgerCategory(ledgerId, categoryId);

        UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", categoryId)
                .eq("ledger_id", ledgerId)
                .set("deleted_at", 1)
                .set("deleted_time", LocalDateTime.now());
        categoryService.update(updateWrapper);
        recordSearchService.refreshCategoryRecordsIfEnabled(existingCategory.getId());
        return ResponseEntity.ok().build();
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

    private void requireLedgerManager(HttpServletRequest request, Long ledgerId) {
        ledgerService.requireLedger(ledgerId);
        if (RequestAuthUtil.isAdmin(request)) {
            return;
        }
        Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
        if (!ledgerService.hasManagementPermission(ledgerId, currentUserId)) {
            throw new ForbiddenException("You do not have permission to manage this ledger's categories");
        }
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

    private void validateCreateRequest(CategoryRequest categoryRequest) {
        if (categoryRequest == null) {
            throw new BadRequestException("Request body is required");
        }
        validateRequiredText(categoryRequest.getName(), "Category name is required");
        validateRequiredText(categoryRequest.getIcon(), "Category icon is required");
        validateRequiredText(categoryRequest.getColor(), "Category color is required");
        validateRequiredText(categoryRequest.getType(), "Category type is required");
    }

    private void validateUpdateRequest(CategoryRequest categoryRequest) {
        if (categoryRequest.getName() != null) {
            validateRequiredText(categoryRequest.getName(), "Category name cannot be blank");
        }
        if (categoryRequest.getIcon() != null) {
            validateRequiredText(categoryRequest.getIcon(), "Category icon cannot be blank");
        }
        if (categoryRequest.getColor() != null) {
            validateRequiredText(categoryRequest.getColor(), "Category color cannot be blank");
        }
        if (categoryRequest.getType() != null) {
            validateRequiredText(categoryRequest.getType(), "Category type cannot be blank");
        }
        if (categoryRequest.getName() == null && categoryRequest.getIcon() == null
                && categoryRequest.getColor() == null && categoryRequest.getType() == null) {
            throw new BadRequestException("At least one category field must be provided");
        }
    }

    private void validateRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
    }

    private String resolveString(String requestedValue, String existingValue) {
        if (!StringUtils.hasText(requestedValue)) {
            return existingValue;
        }
        return requestedValue.trim();
    }

    private String resolveCategoryType(String requestedValue, String existingValue) {
        String normalizedType = RecordTypeNormalizer.normalizeOptional(requestedValue, "Category type must be income or expense");
        return normalizedType == null ? existingValue : normalizedType;
    }

    private String normalizeCategoryType(String value, boolean required) {
        if (!required) {
            return RecordTypeNormalizer.normalizeOptional(value, "Category type must be income or expense");
        }
        return RecordTypeNormalizer.normalizeRequired(value, "Category type is required", "Category type must be income or expense");
    }
}
