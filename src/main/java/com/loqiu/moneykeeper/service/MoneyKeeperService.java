package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.entity.MoneyKeeper;

import java.time.LocalDate;
import java.util.List;

public interface MoneyKeeperService extends IService<MoneyKeeper> {
    // 自定义业务方法
    List<MoneyKeeper> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    List<MoneyKeeper> findByUserIdAndType(Long userId, String type);
    //获取record 带 Category Name
    List<MoneyKeeper> getAllRecordsWithCategoryName();
} 