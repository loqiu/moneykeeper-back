package com.loqiu.moneykeeper.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Money keeper record DTO")
public class MoneyKeeperDTO {

    @Schema(description = "Record id", example = "1", hidden = true)
    @ExcelIgnore
    private Long id;

    @Schema(description = "Ledger id", example = "1", hidden = true)
    @ExcelIgnore
    private Long ledgerId;

    @Schema(description = "User id", example = "1", hidden = true)
    @ExcelIgnore
    private Long userId;

    @Schema(description = "Category id", example = "1", hidden = true)
    @ExcelIgnore
    private Long categoryId;

    @Schema(description = "Category name", example = "Food")
    @ExcelProperty(value = "Category Name", index = 0)
    @ColumnWidth(15)
    private String categoryName;

    @Schema(description = "Record type", example = "expense")
    @ExcelProperty(value = "Type", index = 1)
    @ColumnWidth(10)
    private String type;

    @Schema(description = "Amount", example = "99.99")
    @ExcelProperty(value = "Amount", index = 2)
    @ColumnWidth(15)
    private BigDecimal amount;

    @Schema(description = "Transaction date", example = "2024-01-01")
    @ExcelProperty(value = "Transaction Date", index = 3)
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate transactionDate;

    @Schema(description = "Updated time", example = "2024-01-01 12:00:00")
    @ExcelProperty(value = "Updated At", index = 4)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime updatedAt;

    @Schema(description = "Notes", example = "Lunch with the team")
    @ExcelProperty(value = "Notes", index = 5)
    @ColumnWidth(30)
    private String notes;
}