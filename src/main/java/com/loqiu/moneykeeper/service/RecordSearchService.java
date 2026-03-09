package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.RecordSearchIndexStatsDTO;
import com.loqiu.moneykeeper.dto.RecordSearchReindexResultDTO;
import com.loqiu.moneykeeper.dto.RecordSearchResultDTO;

import java.time.LocalDate;
import java.util.List;

public interface RecordSearchService {
    boolean isEnabled();

    boolean isReady();

    String getIndexName();

    List<RecordSearchResultDTO> searchRecords(Long userId,
                                              String query,
                                              String type,
                                              Long categoryId,
                                              String categoryName,
                                              LocalDate startDate,
                                              LocalDate endDate,
                                              int limit);

    RecordSearchReindexResultDTO reindexRecords(Long userId);

    RecordSearchIndexStatsDTO getIndexStats(Long userId);

    void syncRecordIfEnabled(Long recordId);

    void removeRecordIfEnabled(Long recordId);

    void refreshCategoryRecordsIfEnabled(Long categoryId);

    void reindexUserRecordsIfEnabled(Long userId);
}