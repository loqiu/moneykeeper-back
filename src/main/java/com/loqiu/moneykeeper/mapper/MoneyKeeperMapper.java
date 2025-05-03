package com.loqiu.moneykeeper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

//@Mapper
public interface MoneyKeeperMapper extends BaseMapper<MoneyKeeper> {

    public List<MoneyKeeperDTO> getAllRecordsWithCategoryName(@Param("userId") Long userId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate
    );

    public List<MoneyKeeperDTO> getAllRecordsByCategoryName(@Param("categoryName") String categoryName,
                                                              @Param("userId") Long userId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate
    );

    Map<String, BigDecimal> getMoneyKeeperSummary(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate
    );

    List<MoneyKeeper> getMoneyKeeperRecords(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate
    );

} 