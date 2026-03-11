package com.loqiu.moneykeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordSearchReindexResultDTO {
    private String scope;
    private Long userId;
    private Long ledgerId;
    private Integer indexedCount;
    private String indexName;
    private LocalDateTime reindexedAt;
}