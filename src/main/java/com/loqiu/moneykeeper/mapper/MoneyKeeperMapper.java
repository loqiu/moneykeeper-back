package com.loqiu.moneykeeper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loqiu.moneykeeper.DTO.MoneyKeeperDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;

import java.util.List;

//@Mapper
public interface MoneyKeeperMapper extends BaseMapper<MoneyKeeper> {

    public List<MoneyKeeperDTO> getAllRecordsWithCategoryName();

} 