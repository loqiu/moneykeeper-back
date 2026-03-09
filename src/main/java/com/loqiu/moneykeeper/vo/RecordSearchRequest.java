package com.loqiu.moneykeeper.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RecordSearchRequest {
    private Long userId;
    private String query;
    private String type;
    private Long categoryId;
    private String categoryName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Integer limit = 20;
}