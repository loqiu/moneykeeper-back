package com.loqiu.moneykeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.vo.RecordSummary;

import java.time.LocalDate;
import java.util.List;

public interface MoneyKeeperService extends IService<MoneyKeeper> {
    List<MoneyKeeper> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    List<MoneyKeeper> findByUserIdAndType(Long userId, String type);

    List<MoneyKeeperDTO> getAllRecordsWithCategoryName(Long userId, LocalDate startDate, LocalDate endDate);

    List<MoneyKeeperDTO> getAllLedgerRecordsWithCategoryName(Long ledgerId,
                                                             Long userId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);

    List<MoneyKeeperDTO> getAllRecordsByCategoryName(String categoryName,
                                                     Long userId,
                                                     LocalDate startDate,
                                                     LocalDate endDate);

    RecordSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate);

    Boolean insertMoneyKeeper(MoneyKeeper moneyKeeper);
}