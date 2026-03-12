package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.LedgerStatisticsBucketDTO;
import com.loqiu.moneykeeper.dto.LedgerStatisticsCategoryDTO;
import com.loqiu.moneykeeper.dto.LedgerStatisticsDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.LedgerStatisticsService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerStatisticsControllerTest {

    @Mock
    private LedgerStatisticsService ledgerStatisticsService;

    @Mock
    private LedgerService ledgerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LedgerStatisticsController controller = new LedgerStatisticsController();
        ReflectionTestUtils.setField(controller, "ledgerStatisticsService", ledgerStatisticsService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getStatisticsShouldReturnLedgerStatistics() throws Exception {
        LedgerStatisticsDTO statistics = LedgerStatisticsDTO.builder()
                .ledgerId(31L)
                .userId(2L)
                .period("month")
                .bucketGranularity("day")
                .anchorDate(LocalDate.of(2026, 3, 12))
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .previousStartDate(LocalDate.of(2026, 2, 1))
                .previousEndDate(LocalDate.of(2026, 2, 28))
                .totalIncome(new BigDecimal("200.00"))
                .totalExpense(new BigDecimal("60.00"))
                .balance(new BigDecimal("140.00"))
                .recordCount(4)
                .incomeRecordCount(1)
                .expenseRecordCount(3)
                .incomeDelta(new BigDecimal("50.00"))
                .expenseDelta(new BigDecimal("45.00"))
                .balanceDelta(new BigDecimal("5.00"))
                .incomeChangePercentage(new BigDecimal("33.33"))
                .expenseChangePercentage(new BigDecimal("300.00"))
                .balanceChangePercentage(new BigDecimal("3.70"))
                .buckets(List.of(LedgerStatisticsBucketDTO.builder()
                        .bucketKey("2026-03-10")
                        .label("03-10")
                        .startDate(LocalDate.of(2026, 3, 10))
                        .endDate(LocalDate.of(2026, 3, 10))
                        .totalIncome(new BigDecimal("200.00"))
                        .totalExpense(new BigDecimal("10.00"))
                        .balance(new BigDecimal("190.00"))
                        .recordCount(2)
                        .build()))
                .expenseCategories(List.of(LedgerStatisticsCategoryDTO.builder()
                        .categoryId(9L)
                        .categoryName("Coffee")
                        .type("expense")
                        .totalAmount(new BigDecimal("40.00"))
                        .percentage(new BigDecimal("66.67"))
                        .recordCount(2)
                        .build()))
                .incomeCategories(List.of(LedgerStatisticsCategoryDTO.builder()
                        .categoryId(10L)
                        .categoryName("Salary")
                        .type("income")
                        .totalAmount(new BigDecimal("200.00"))
                        .percentage(new BigDecimal("100.00"))
                        .recordCount(1)
                        .build()))
                .build();

        when(ledgerService.hasActiveMembership(31L, 5L)).thenReturn(true);
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(ledgerStatisticsService.getStatistics(31L, "month", LocalDate.of(2026, 3, 12), 2L)).thenReturn(statistics);

        mockMvc.perform(get("/api/ledgers/31/statistics")
                        .param("period", "month")
                        .param("anchorDate", "2026-03-12")
                        .param("userId", "2")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 5L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ledgerId").value(31))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.period").value("month"))
                .andExpect(jsonPath("$.totalExpense").value(60.00))
                .andExpect(jsonPath("$.buckets[0].bucketKey").value("2026-03-10"))
                .andExpect(jsonPath("$.expenseCategories[0].categoryName").value("Coffee"))
                .andExpect(jsonPath("$.expenseCategories[0].percentage").value(66.67));

        verify(ledgerStatisticsService).getStatistics(31L, "month", LocalDate.of(2026, 3, 12), 2L);
    }

    @Test
    void getStatisticsShouldRejectInactiveTargetUser() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 5L)).thenReturn(true);
        when(ledgerService.hasActiveMembership(31L, 99L)).thenReturn(false);

        mockMvc.perform(get("/api/ledgers/31/statistics")
                        .param("userId", "99")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 5L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Target user is not an active member of this ledger"));
    }
}
