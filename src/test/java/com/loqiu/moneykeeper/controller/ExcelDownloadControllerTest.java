package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExcelDownloadControllerTest {

    @Mock
    private MoneyKeeperService moneyKeeperService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ExcelDownloadController controller = new ExcelDownloadController();
        ReflectionTestUtils.setField(controller, "moneyKeeperService", moneyKeeperService);
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
}