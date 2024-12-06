package com.loqiu.moneykeeper.DTO;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "记账记录DTO", description = "记账记录数据传输对象，包含记账的详细信息")
public class MoneyKeeperDTO {

    @ApiModelProperty(value = "记录ID", example = "1", hidden = true)
    @ExcelIgnore
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1", hidden = true)
    @ExcelIgnore
    private Long userId;

    @ApiModelProperty(value = "分类ID", example = "1", hidden = true)
    @ExcelIgnore
    private Long categoryId;

    @ApiModelProperty(value = "分类名称", example = "餐饮", notes = "消费类别的名称")
    @ExcelProperty(value = "分类名称", index = 0)
    @ColumnWidth(15)
    private String categoryName;

    @ApiModelProperty(value = "类型", example = "支出", notes = "交易类型：支出/收入")
    @ExcelProperty(value = "类型", index = 1)
    @ColumnWidth(10)
    private String type;

    @ApiModelProperty(value = "金额", example = "99.99", notes = "交易金额")
    @ExcelProperty(value = "金额", index = 2)
    @ColumnWidth(15)
    private BigDecimal amount;

    @ApiModelProperty(value = "交易日期", example = "2024-01-01", notes = "记账日期")
    @ExcelProperty(value = "交易日期", index = 3)
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate transactionDate;
    
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00", notes = "记录最后更新时间")
    @ExcelProperty(value = "更新时间", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "备注", example = "午餐费用", notes = "记账备注信息")
    @ExcelProperty(value = "备注", index = 5)
    @ColumnWidth(30)
    private String notes;
}