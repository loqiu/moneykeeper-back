package com.loqiu.moneykeeper.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LogManager.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private RecordSearchService recordSearchService;

    @PostMapping("/{id}")
    public ResponseEntity<Category> createCategory(@PathVariable Long id,
                                                   @RequestBody CategoryRequest categoryRequest,
                                                   HttpServletRequest request) {
        logger.info("Creating category - targetUserId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, id);
        validateCreateRequest(categoryRequest);

        Category category = new Category();
        category.setUserId(id);
        category.setLedgerId(ledgerService.getOrCreatePersonalLedger(id).getId());
        category.setName(categoryRequest.getName().trim());
        category.setIcon(categoryRequest.getIcon().trim());
        category.setColor(categoryRequest.getColor().trim());
        category.setType(normalizeCategoryType(categoryRequest.getType(), true));

        categoryService.insertCategory(category);
        logger.info("Category created successfully - userId: {}, categoryName: {}", id, category.getName());
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Getting category - categoryId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        Category category = requireCategory(id);
        requireSelfOrAdmin(request, category.getUserId());
        return ResponseEntity.ok(category);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getCategoriesByUserId(@PathVariable Long userId, HttpServletRequest request) {
        logger.info("Getting categories by userId - targetUserId: {}, currentUserId: {}", userId, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId);
        return ResponseEntity.ok(categoryService.findByUserId(userId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(@PathVariable String type, HttpServletRequest request) {
        logger.info("Getting categories by type - type: {}, currentUserId: {}", type, RequestAuthUtil.getCurrentUserId(request));
        validateRequiredText(type, "Category type is required");

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", normalizeCategoryType(type, true))
                .orderByDesc("created_at");
        if (!RequestAuthUtil.isAdmin(request)) {
            queryWrapper.eq("user_id", RequestAuthUtil.requireCurrentUserId(request));
        }
        return ResponseEntity.ok(categoryService.list(queryWrapper));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @RequestBody CategoryRequest categoryRequest,
                                                   HttpServletRequest request) {
        logger.info("Updating category - categoryId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        if (categoryRequest == null) {
            throw new BadRequestException("Request body is required");
        }

        Category existingCategory = requireCategory(id);
        requireSelfOrAdmin(request, existingCategory.getUserId());
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
        logger.info("Category updated successfully - categoryId: {}", id);
        return ResponseEntity.ok(requireCategory(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Deleting category - categoryId: {}, currentUserId: {}", id, RequestAuthUtil.getCurrentUserId(request));
        Category deletedCategory = requireCategory(id);
        requireSelfOrAdmin(request, deletedCategory.getUserId());
        if (deletedCategory.getDeletedAt() != null && deletedCategory.getDeletedAt() == 1) {
            throw new ResourceNotFoundException("Category not found");
        }

        UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("deleted_at", 1)
                .set("deleted_time", LocalDateTime.now());
        categoryService.update(updateWrapper);
        recordSearchService.refreshCategoryRecordsIfEnabled(deletedCategory.getId());
        logger.info("Category deleted successfully - categoryId: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<Category>> getAllCategories(HttpServletRequest request) {
        logger.info("Getting all categories - currentUserId: {}", RequestAuthUtil.getCurrentUserId(request));
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        if (!RequestAuthUtil.isAdmin(request)) {
            queryWrapper.eq("user_id", RequestAuthUtil.requireCurrentUserId(request));
        }
        queryWrapper.orderByDesc("created_at");
        return ResponseEntity.ok(categoryService.list(queryWrapper));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByUserIdAndType(@PathVariable Long userId,
                                                                       @PathVariable String type,
                                                                       HttpServletRequest request) {
        logger.info("Getting categories by userId and type - targetUserId: {}, type: {}, currentUserId: {}",
                userId, type, RequestAuthUtil.getCurrentUserId(request));
        requireSelfOrAdmin(request, userId);
        validateRequiredText(type, "Category type is required");

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("type", normalizeCategoryType(type, true))
                .orderByDesc("created_at");
        return ResponseEntity.ok(categoryService.list(queryWrapper));
    }

    @GetMapping("/list/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(@PathVariable String type,
                                                              @RequestParam(required = false) Long userId,
                                                              HttpServletRequest request) {
        logger.info("Getting categories by type with optional user filter - type: {}, targetUserId: {}, currentUserId: {}",
                type, userId, RequestAuthUtil.getCurrentUserId(request));
        validateRequiredText(type, "Category type is required");

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", normalizeCategoryType(type, true))
                .orderByDesc("created_at");

        if (RequestAuthUtil.isAdmin(request)) {
            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
        } else {
            Long currentUserId = RequestAuthUtil.requireCurrentUserId(request);
            if (userId != null && !currentUserId.equals(userId)) {
                throw new ForbiddenException("You do not have permission to access this user's categories");
            }
            queryWrapper.eq("user_id", currentUserId);
        }

        return ResponseEntity.ok(categoryService.list(queryWrapper));
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

    private void requireSelfOrAdmin(HttpServletRequest request, Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
        if (!RequestAuthUtil.isSelfOrAdmin(request, userId)) {
            throw new ForbiddenException("You do not have permission to access this category");
        }
    }

    private Category requireCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BadRequestException("Category id is required");
        }
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private String resolveString(String requestedValue, String existingValue) {
        if (!StringUtils.hasText(requestedValue)) {
            return existingValue;
        }
        return requestedValue.trim();
    }

    private String resolveCategoryType(String requestedValue, String existingValue) {
        String normalizedType = RecordTypeNormalizer.normalizeOptional(requestedValue, "Category type must be income or expense", ErrorKeyConstants.CATEGORY_INVALID_TYPE);
        return normalizedType == null ? existingValue : normalizedType;
    }

    private String normalizeCategoryType(String value, boolean required) {
        if (!required) {
            return RecordTypeNormalizer.normalizeOptional(value, "Category type must be income or expense", ErrorKeyConstants.CATEGORY_INVALID_TYPE);
        }
        return RecordTypeNormalizer.normalizeRequired(value, "Category type is required", "Category type must be income or expense", ErrorKeyConstants.CATEGORY_INVALID_TYPE);
    }
}
