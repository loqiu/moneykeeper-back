package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.LedgerStatisticsDTO;

import java.time.LocalDate;

public interface LedgerStatisticsService {
    LedgerStatisticsDTO getStatistics(Long ledgerId, String period, LocalDate anchorDate, Long userId);
}
