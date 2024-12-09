package com.loqiu.moneykeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loqiu.moneykeeper.DTO.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.mapper.MoneyKeeperMapper;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.vo.RecordSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MoneyKeeperServiceImpl extends ServiceImpl<MoneyKeeperMapper, MoneyKeeper> implements MoneyKeeperService {


    @Autowired
    private MoneyKeeperMapper moneyKeeperMapper;

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
    public List<MoneyKeeperDTO> getAllRecordsWithCategoryName(Long userId, LocalDate startDate, LocalDate endDate) {
        List<MoneyKeeperDTO> result = moneyKeeperMapper.getAllRecordsWithCategoryName(userId, startDate, endDate);
        return result;
    }

    @Override
    public RecordSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        // 获取汇总数据
        Map<String, BigDecimal> summary = moneyKeeperMapper.getMoneyKeeperSummary(userId, startDate, endDate);

        return RecordSummary.builder()
                .totalIncome(summary.get("totalIncome"))
                .totalExpense(summary.get("totalExpense"))
                .balance(summary.get("balance"))
                .build();
    }
} 