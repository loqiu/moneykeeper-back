package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.mapper.CategoryMapper;
import com.loqiu.moneykeeper.mapper.MoneyKeeperMapper;
import com.loqiu.moneykeeper.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private MoneyKeeperMapper moneyKeeperMapper;

    @Override
    public List<Category> findByUserId(Long userId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return list(queryWrapper);
    }
    
    @Override
    public List<Category> findByType(String type) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type);
        return list(queryWrapper);
    }

    @Override
    public Boolean insertCategory(Category category) {
        Boolean result = moneyKeeperMapper.insertCategory(category);
        return result;
    }
} 