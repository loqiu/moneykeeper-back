package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.vo.RecordSummary;

import java.time.LocalDate;
import java.util.List;

public interface MoneyKeeperService extends IService<MoneyKeeper> {
    // 自定义业务方法
    List<MoneyKeeper> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    List<MoneyKeeper> findByUserIdAndType(Long userId, String type);
    //获取record 带 Category Name
    List<MoneyKeeperDTO> getAllRecordsWithCategoryName(Long userId, LocalDate startDate, LocalDate endDate);
    List<MoneyKeeperDTO> getAllRecordsByCategoryName(String CategoryName,Long userId, LocalDate startDate, LocalDate endDate);
    /**
     * 获取收支汇总信息
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收支汇总信息
     */
    RecordSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate);
    Boolean insertMoneyKeeper(MoneyKeeper moneyKeeper);
} 