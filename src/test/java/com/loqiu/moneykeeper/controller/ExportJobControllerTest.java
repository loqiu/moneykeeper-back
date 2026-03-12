package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.ExportJobDTO;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.ExportJobService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExportJobControllerTest {

    @Mock
    private ExportJobService exportJobService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private MoneyKeeperService moneyKeeperService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ExportJobController controller = new ExportJobController();
        ReflectionTestUtils.setField(controller, "exportJobService", exportJobService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(controller, "moneyKeeperService", moneyKeeperService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createJobShouldRejectInvalidDateRange() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/ledgers/31/export-jobs")
                        .contentType("application/json")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "startDate": "2026-03-09",
                                  "endDate": "2026-03-08"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date cannot be before start date"));
    }

    @Test
    void listJobsShouldUseCurrentUserForMember() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(exportJobService.listJobs(31L, 2L, false, 10)).thenReturn(List.of());

        mockMvc.perform(get("/api/ledgers/31/export-jobs")
                        .param("limit", "10")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk());

        verify(exportJobService).listJobs(31L, 2L, false, 10);
    }

    @Test
    void downloadJobShouldReturnWorkbookForRequester() throws Exception {
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(exportJobService.getJob(31L, 7L, 2L, false)).thenReturn(ExportJobDTO.builder()
                .id(7L)
                .ledgerId(31L)
                .requestedByUserId(2L)
                .fileName("ledger_31_records_2026-03-12.xlsx")
                .build());
        when(moneyKeeperService.getAllLedgerRecordsWithCategoryName(31L, null, null, null)).thenReturn(List.<MoneyKeeperDTO>of());

        mockMvc.perform(get("/api/ledgers/31/export-jobs/7/download")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")));

        verify(exportJobService).markJobDownloaded(7L);
    }
}
