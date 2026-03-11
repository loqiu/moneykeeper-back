package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.RecordSearchIndexStatsDTO;
import com.loqiu.moneykeeper.dto.RecordSearchReindexResultDTO;
import com.loqiu.moneykeeper.dto.RecordSearchResultDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.exception.ServiceUnavailableException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecordSearchControllerTest {

    @Mock
    private RecordSearchService recordSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RecordSearchController controller = new RecordSearchController();
        ReflectionTestUtils.setField(controller, "recordSearchService", recordSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchRecordsShouldUseCurrentUserWhenUserIdIsOmitted() throws Exception {
        when(recordSearchService.searchRecords(1L, "lunch", null, null, null, null, null, 20))
                .thenReturn(List.of(
                        RecordSearchResultDTO.builder()
                                .id(11L)
                                .ledgerId(21L)
                                .userId(1L)
                                .categoryId(5L)
                                .categoryName("Food")
                                .type("expense")
                                .amount(new BigDecimal("18.50"))
                                .transactionDate(LocalDate.of(2026, 3, 8))
                                .updatedAt(LocalDateTime.of(2026, 3, 8, 12, 0))
                                .notes("Lunch with team")
                                .score(1.25)
                                .build()
                ));

        mockMvc.perform(get("/api/search/records")
                        .param("query", "lunch")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].ledgerId").value(21))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].score").value(1.25));

        verify(recordSearchService).searchRecords(1L, "lunch", null, null, null, null, null, 20);
    }

    @Test
    void searchRecordsShouldRejectOtherUsersForNormalUser() throws Exception {
        mockMvc.perform(get("/api/search/records")
                        .param("userId", "2")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to search this user's records"));
    }

    @Test
    void searchRecordsShouldReturnServiceUnavailableWhenModuleIsDisabled() throws Exception {
        when(recordSearchService.searchRecords(1L, "budget", null, null, null, null, null, 20))
                .thenThrow(new ServiceUnavailableException("Elasticsearch search module is disabled"));

        mockMvc.perform(get("/api/search/records")
                        .param("query", "budget")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Elasticsearch search module is disabled"));
    }

    @Test
    void reindexRecordsShouldRequireAdmin() throws Exception {
        mockMvc.perform(post("/api/search/records/reindex")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role is required"));
    }

    @Test
    void reindexRecordsShouldReturnResultForAdmin() throws Exception {
        when(recordSearchService.reindexRecords(null)).thenReturn(
                RecordSearchReindexResultDTO.builder()
                        .scope("all")
                        .userId(null)
                        .ledgerId(null)
                        .indexedCount(12)
                        .indexName("moneykeeper-records")
                        .reindexedAt(LocalDateTime.of(2026, 3, 8, 12, 0))
                        .build()
        );

        mockMvc.perform(post("/api/search/records/reindex")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("all"))
                .andExpect(jsonPath("$.indexedCount").value(12))
                .andExpect(jsonPath("$.indexName").value("moneykeeper-records"));
    }

    @Test
    void reindexLedgerRecordsShouldReturnResultForAdmin() throws Exception {
        when(recordSearchService.reindexLedgerRecords(31L)).thenReturn(
                RecordSearchReindexResultDTO.builder()
                        .scope("ledger")
                        .ledgerId(31L)
                        .indexedCount(8)
                        .indexName("moneykeeper-records")
                        .reindexedAt(LocalDateTime.of(2026, 3, 11, 20, 0))
                        .build()
        );

        mockMvc.perform(post("/api/search/records/reindex/ledger")
                        .param("ledgerId", "31")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ledger"))
                .andExpect(jsonPath("$.ledgerId").value(31))
                .andExpect(jsonPath("$.indexedCount").value(8));
    }

    @Test
    void getIndexStatsShouldRequireAdmin() throws Exception {
        mockMvc.perform(get("/api/search/records/stats")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role is required"));
    }

    @Test
    void getIndexStatsShouldReturnStatsForAdmin() throws Exception {
        when(recordSearchService.getIndexStats(2L)).thenReturn(
                RecordSearchIndexStatsDTO.builder()
                        .scope("user")
                        .userId(2L)
                        .enabled(true)
                        .ready(true)
                        .indexName("moneykeeper-records")
                        .indexExists(true)
                        .indexedDocumentCount(8L)
                        .databaseRecordCount(9L)
                        .statsCollectedAt(LocalDateTime.of(2026, 3, 9, 9, 0))
                        .build()
        );

        mockMvc.perform(get("/api/search/records/stats")
                        .param("userId", "2")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("user"))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.indexExists").value(true))
                .andExpect(jsonPath("$.indexedDocumentCount").value(8))
                .andExpect(jsonPath("$.databaseRecordCount").value(9));
    }

    @Test
    void getLedgerIndexStatsShouldReturnStatsForAdmin() throws Exception {
        when(recordSearchService.getLedgerIndexStats(31L)).thenReturn(
                RecordSearchIndexStatsDTO.builder()
                        .scope("ledger")
                        .ledgerId(31L)
                        .enabled(true)
                        .ready(true)
                        .indexName("moneykeeper-records")
                        .indexExists(true)
                        .indexedDocumentCount(15L)
                        .databaseRecordCount(15L)
                        .statsCollectedAt(LocalDateTime.of(2026, 3, 11, 20, 30))
                        .build()
        );

        mockMvc.perform(get("/api/search/records/stats/ledger")
                        .param("ledgerId", "31")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ledger"))
                .andExpect(jsonPath("$.ledgerId").value(31))
                .andExpect(jsonPath("$.indexedDocumentCount").value(15))
                .andExpect(jsonPath("$.databaseRecordCount").value(15));
    }

    @Test
    void getIndexStatsShouldReturnServiceUnavailableWhenModuleIsDisabled() throws Exception {
        when(recordSearchService.getIndexStats(null))
                .thenThrow(new ServiceUnavailableException("Elasticsearch search module is disabled"));

        mockMvc.perform(get("/api/search/records/stats")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "admin"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Elasticsearch search module is disabled"));
    }
}