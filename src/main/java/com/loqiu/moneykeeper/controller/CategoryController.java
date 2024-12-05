package com.loqiu.moneykeeper.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.service.CategoryService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LogManager.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;
    
    @PostMapping("/{id}")
    public ResponseEntity<Category> createCategory(@PathVariable Long id, @RequestBody Category category) {
        logger.info("Creating category - Input - userId: {}, category: {}", id, category);
        
        if (id == null || category == null) {
            logger.error("Invalid input - userId: {}, category: {}", id, category);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            category.setUserId(id);
            categoryService.save(category);
            logger.info("Category created successfully - Output - category: {}", category);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            logger.error("Failed to create category - userId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        logger.info("Getting category - Input - categoryId: {}", id);
        
        if (id == null) {
            logger.error("Invalid input - categoryId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Category category = categoryService.getById(id);
            if (category != null) {
                logger.info("Category found - Output - category: {}", category);
                return ResponseEntity.ok(category);
            } else {
                logger.warn("Category not found - categoryId: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get category - categoryId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getCategoriesByUserId(@PathVariable Long userId) {
        logger.info("Getting categories by userId - Input - userId: {}", userId);
        
        if (userId == null) {
            logger.error("Invalid input - userId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<Category> categories = categoryService.findByUserId(userId);
            logger.info("Categories found - Output - count: {}, categories: {}", 
                categories.size(), categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to get categories - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(@PathVariable String type) {
        logger.info("Getting categories by type - Input - type: {}", type);
        
        if (type == null || type.trim().isEmpty()) {
            logger.error("Invalid input - type is null or empty");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<Category> categories = categoryService.findByType(type);
            logger.info("Categories found - Output - count: {}, categories: {}", 
                categories.size(), categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to get categories - type: {}, error: {}", type, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        logger.info("Updating category - Input - categoryId: {}, category: {}", id, category);
        
        if (id == null || category == null) {
            logger.error("Invalid input - categoryId: {}, category: {}", id, category);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Category existingCategory = categoryService.getById(id);
            if (existingCategory == null) {
                logger.warn("Category not found - categoryId: {}", id);
                return ResponseEntity.notFound().build();
            }
            if(null == category.getId()){
                category.setId(id);
            }
            logger.info("Category found - categoryId: {}, existingCategory: {}, updateCategory:{}", id, existingCategory,category);
            categoryService.updateById(category);
            logger.info("Category updated successfully - Output - category: {}", category);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            logger.error("Failed to update category - categoryId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting category - Input - categoryId: {}", id);
        
        if (id == null) {
            logger.error("Invalid input - categoryId is null");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Category deletedCategory = categoryService.getById(id);
            logger.info("Category found - categoryId: {}, deletedCategory: {}", id, deletedCategory);
            if(deletedCategory != null && deletedCategory.getDeletedAt() == 1){
                logger.warn("Category have already deleted - categoryId: {}", id);
                return ResponseEntity.notFound().build();
            }
            LocalDateTime currentTime = LocalDateTime.now();
            UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .set("deleted_at", 1)
                    .set("deleted_time", currentTime);
            logger.info("Category updateWrapper - categoryId: {}, updateWrapper: {}", id, updateWrapper);
            categoryService.update(updateWrapper);
            logger.info("Category deleted successfully - categoryId: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to delete category - categoryId: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Category>> getAllCategories() {
        logger.info("Getting all categories");
        
        try {
            List<Category> categories = categoryService.list();
            logger.info("Categories found - Output - count: {}", categories.size());
            logger.debug("Categories details: {}", categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to get categories list - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByUserIdAndType(
            @PathVariable Long userId,
            @PathVariable String type) {
        logger.info("Getting categories by userId and type - Input - userId: {}, type: {}", userId, type);
        
        if (userId == null || type == null || type.trim().isEmpty()) {
            logger.error("Invalid input - userId: {}, type: {}", userId, type);
            return ResponseEntity.badRequest().build();
        }
        
        try {
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("type", type)
                       .eq("deleted_at", 0)
                       .orderByDesc("created_at");
            
            List<Category> categories = categoryService.list(queryWrapper);
            logger.info("Categories found - Output - count: {}", categories.size());
            logger.debug("Categories details: {}", categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to get categories - userId: {}, type: {}, error: {}", 
                userId, type, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(
            @PathVariable String type,
            @RequestParam(required = false) Long userId) {
        logger.info("Getting categories by type - Input - type: {}, userId: {}", type, userId);
        
        if (type == null || type.trim().isEmpty()) {
            logger.error("Invalid input - type is null or empty");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type", type)
                       .eq("deleted_at", 0);
            
            if (userId != null) {
                queryWrapper.and(wrapper -> wrapper
                    .eq("user_id", userId));
                // 系统默认分类
            }
            
            queryWrapper.orderByDesc("created_at");
            
            List<Category> categories = categoryService.list(queryWrapper);
            logger.info("Categories found - Output - count: {}", categories.size());
            logger.debug("Categories details: {}", categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to get categories - type: {}, userId: {}, error: {}", 
                type, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 