package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.mapper.MoneyKeeperMapper;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MoneyKeeperServiceImpl extends ServiceImpl<MoneyKeeperMapper, MoneyKeeper> implements MoneyKeeperService {
    
    @Override
    public List<MoneyKeeper> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .ge("transaction_date", startDate)
                   .le("transaction_date", endDate)
                   .orderByDesc("transaction_date");
        return list(queryWrapper);
    }
    
    @Override
    public List<MoneyKeeper> findByUserIdAndType(Long userId, String type) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("type", type)
                   .orderByDesc("transaction_date");
        return list(queryWrapper);
    }

    @Override
    public List<MoneyKeeper> getAllRecordsWithCategoryName() {
        return null;
    }
} 