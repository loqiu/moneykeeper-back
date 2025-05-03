package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    // 自定义业务方法
    List<Category> findByUserId(Long userId);
    List<Category> findByType(String type);
    Boolean insertCategory(Category category);
} 