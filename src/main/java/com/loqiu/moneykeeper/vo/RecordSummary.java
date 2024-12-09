package com.loqiu.moneykeeper.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordSummary {
    private BigDecimal totalIncome;    // 总收入
    private BigDecimal totalExpense;   // 总支出
    private BigDecimal balance;        // 结余
} 