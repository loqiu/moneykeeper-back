package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExcelDownloadControllerTest {

    @Mock
    private MoneyKeeperService moneyKeeperService;

    @Mock
    private LedgerService ledgerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ExcelDownloadController controller = new ExcelDownloadController();
        ReflectionTestUtils.setField(controller, "moneyKeeperService", moneyKeeperService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void downloadUserRecordsShouldRejectInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/excel/download/1")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .param("startDate", "2026-03-09")
                        .param("endDate", "2026-03-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date cannot be before start date"));
    }

    @Test
    void downloadLedgerRecordsShouldRejectNonMember() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(false);

        mockMvc.perform(get("/api/excel/ledgers/31/download")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to export this ledger's records"));
    }

    @Test
    void downloadLedgerRecordsShouldReturnWorkbookForMember() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(31L, null, null, null)).thenReturn(List.<MoneyKeeperDTO>of());

        mockMvc.perform(get("/api/excel/ledgers/31/download")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")));

        verify(moneyKeeperService).getAllLedgerRecordsWithCategoryName(31L, null, null, null);
    }
}