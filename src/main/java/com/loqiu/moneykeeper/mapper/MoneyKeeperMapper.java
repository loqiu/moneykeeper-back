package com.loqiu.moneykeeper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MoneyKeeperMapper extends BaseMapper<MoneyKeeper> {

    List<MoneyKeeperDTO> getAllRecordsWithCategoryName(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    List<MoneyKeeperDTO> getLedgerRecordsWithCategoryName(@Param("ledgerId") Long ledgerId,
                                                          @Param("userId") Long userId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    List<MoneyKeeperDTO> getAllRecordsByCategoryName(@Param("categoryName") String categoryName,
                                                     @Param("userId") Long userId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    MoneyKeeperDTO getRecordWithCategoryName(@Param("recordId") Long recordId);

    List<MoneyKeeperDTO> getRecordsWithCategoryNameByCategoryId(@Param("categoryId") Long categoryId);

    Map<String, BigDecimal> getMoneyKeeperSummary(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    List<MoneyKeeper> getMoneyKeeperRecords(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    Boolean insertCategory(Category category);

    Boolean insertMoneyKeeper(MoneyKeeper moneyKeeper);
}