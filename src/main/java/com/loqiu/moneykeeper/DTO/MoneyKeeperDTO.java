package com.loqiu.moneykeeper.DTO;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MoneyKeeperDTO {

    @ExcelIgnore
    private Long id;

    @ExcelIgnore
    private Long userId;

    @ExcelIgnore
    private Long categoryId;

    @ExcelProperty(value = "分类名称", index = 0)
    @ColumnWidth(15)
    private String categoryName;

    @ExcelProperty(value = "类型", index = 1)
    @ColumnWidth(10)
    private String type;

    @ExcelProperty(value = "金额", index = 2)
    @ColumnWidth(15)
    private BigDecimal amount;

    @ExcelProperty(value = "交易日期", index = 3)
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate transactionDate;
    
    @ExcelProperty(value = "更新时间", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime updatedAt;

    @ExcelProperty(value = "备注", index = 5)
    @ColumnWidth(30)
    private String notes;
}
