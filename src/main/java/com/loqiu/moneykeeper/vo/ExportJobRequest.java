package com.loqiu.moneykeeper.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobRequest {
    private Long userId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
}
