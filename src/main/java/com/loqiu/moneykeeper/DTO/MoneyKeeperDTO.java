package com.loqiu.moneykeeper.DTO;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(description = "记账记录DTO")
public class MoneyKeeperDTO {

    @Schema(description = "记录ID", example = "1", hidden = true)
    @ExcelIgnore
    private Long id;

    @Schema(description = "用户ID", example = "1", hidden = true)
    @ExcelIgnore
    private Long userId;

    @Schema(description = "分类ID", example = "1", hidden = true)
    @ExcelIgnore
    private Long categoryId;

    @Schema(description = "分类名称", example = "餐饮" )
    @ExcelProperty(value = "分类名称", index = 0)
    @ColumnWidth(15)
    private String categoryName;

    @Schema(description = "类型", example = "支出")
    @ExcelProperty(value = "类型", index = 1)
    @ColumnWidth(10)
    private String type;

    @Schema(description = "金额", example = "99.99" )
    @ExcelProperty(value = "金额", index = 2)
    @ColumnWidth(15)
    private BigDecimal amount;

    @Schema(description = "交易日期", example = "2024-01-01" )
    @ExcelProperty(value = "交易日期", index = 3)
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate transactionDate;
    
    @Schema(description = "更新时间", example = "2024-01-01 12:00:00" )
    @ExcelProperty(value = "更新时间", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime updatedAt;

    @Schema(description = "备注", example = "午餐费用" )
    @ExcelProperty(value = "备注", index = 5)
    @ColumnWidth(30)
    private String notes;
}