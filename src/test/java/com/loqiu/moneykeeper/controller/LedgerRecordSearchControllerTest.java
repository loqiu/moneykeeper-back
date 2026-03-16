package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.RecordSearchResultDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.RecordSearchService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerRecordSearchControllerTest {

    @Mock
    private RecordSearchService recordSearchService;

    @Mock
    private LedgerService ledgerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LedgerRecordSearchController controller = new LedgerRecordSearchController();
        ReflectionTestUtils.setField(controller, "recordSearchService", recordSearchService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchLedgerRecordsShouldUseLedgerScope() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(recordSearchService.searchLedgerRecords(31L, null, "team", null, null, null, null, null, 20))
                .thenReturn(List.of(
                        RecordSearchResultDTO.builder()
                                .id(11L)
                                .ledgerId(31L)
                                .userId(2L)
                                .categoryId(5L)
                                .categoryName("Food")
                                .type("expense")
                                .amount(new BigDecimal("18.50"))
                                .transactionDate(LocalDate.of(2026, 3, 11))
                                .updatedAt(LocalDateTime.of(2026, 3, 11, 12, 0))
                                .notes("Team lunch")
                                .score(1.2)
                                .build()
                ));

        mockMvc.perform(get("/api/ledgers/31/search/records")
                        .param("query", "team")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].ledgerId").value(31))
                .andExpect(jsonPath("$[0].categoryName").value("Food"));

        verify(recordSearchService).searchLedgerRecords(31L, null, "team", null, null, null, null, null, 20);
    }

    @Test
    void searchLedgerRecordsShouldRejectNonMember() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(false);

        mockMvc.perform(get("/api/ledgers/31/search/records")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to search this ledger"));
    }

    @Test
    void searchLedgerRecordsShouldRejectInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/ledgers/31/search/records")
                        .param("startDate", "2026-03-12")
                        .param("endDate", "2026-03-11")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date cannot be before start date"));
    }

    @Test
    void searchLedgerRecordsShouldRejectChineseTypeFilter() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);

        mockMvc.perform(get("/api/ledgers/31/search/records")
                        .param("type", "支出")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Record type must be income or expense"));
    }
}
