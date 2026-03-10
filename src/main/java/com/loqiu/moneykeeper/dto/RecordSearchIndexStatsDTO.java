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
public class RecordSearchIndexStatsDTO {
    private String scope;
    private Long userId;
    private boolean enabled;
    private boolean ready;
    private String indexName;
    private boolean indexExists;
    private long indexedDocumentCount;
    private long databaseRecordCount;
    private LocalDateTime statsCollectedAt;
}