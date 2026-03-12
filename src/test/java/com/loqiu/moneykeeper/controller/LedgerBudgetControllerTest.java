package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.BudgetProgressDTO;
import com.loqiu.moneykeeper.dto.BudgetRuleDTO;
import com.loqiu.moneykeeper.dto.LedgerBudgetDTO;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.BudgetService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerBudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private LedgerService ledgerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LedgerBudgetController controller = new LedgerBudgetController();
        ReflectionTestUtils.setField(controller, "budgetService", budgetService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listBudgetsShouldReturnLedgerBudgets() throws Exception {
        LedgerBudgetDTO budget = LedgerBudgetDTO.builder()
                .id(41L)
                .ledgerId(31L)
                .name("March Coffee Budget")
                .periodType("monthly")
                .budgetYear(2026)
                .budgetMonth(3)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .type("expense")
                .amount(new BigDecimal("100.00"))
                .progress(BudgetProgressDTO.builder()
                        .spentAmount(new BigDecimal("23.45"))
                        .remainingAmount(new BigDecimal("76.55"))
                        .usagePercentage(new BigDecimal("23.45"))
                        .exceeded(false)
                        .triggeredThresholdPercentages(List.of())
                        .build())
                .build();

        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(budgetService.listBudgets(31L, 2026, 3, "expense", null)).thenReturn(List.of(budget));

        mockMvc.perform(get("/api/ledgers/31/budgets")
                        .param("year", "2026")
                        .param("month", "3")
                        .param("type", "expense")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(41))
                .andExpect(jsonPath("$[0].ledgerId").value(31))
                .andExpect(jsonPath("$[0].progress.spentAmount").value(23.45));
    }

    @Test
    void createBudgetShouldRequireLedgerManager() throws Exception {
        when(ledgerService.hasManagementPermission(31L, 2L)).thenReturn(false);

        mockMvc.perform(post("/api/ledgers/31/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "name": "March Budget",
                                  "type": "expense",
                                  "amount": 200,
                                  "budgetYear": 2026,
                                  "budgetMonth": 3
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to manage this ledger's budgets"));
    }

    @Test
    void createBudgetShouldReturnCreatedBudget() throws Exception {
        LedgerBudgetDTO budget = LedgerBudgetDTO.builder()
                .id(41L)
                .ledgerId(31L)
                .name("March Budget")
                .periodType("monthly")
                .budgetYear(2026)
                .budgetMonth(3)
                .type("expense")
                .amount(new BigDecimal("200.00"))
                .build();

        when(ledgerService.hasManagementPermission(31L, 1L)).thenReturn(true);
        when(budgetService.createBudget(org.mockito.ArgumentMatchers.eq(31L), org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenReturn(budget);

        mockMvc.perform(post("/api/ledgers/31/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "name": "March Budget",
                                  "type": "expense",
                                  "amount": 200,
                                  "budgetYear": 2026,
                                  "budgetMonth": 3,
                                  "notes": "Team coffee"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(41))
                .andExpect(jsonPath("$.name").value("March Budget"))
                .andExpect(jsonPath("$.amount").value(200.00));
    }

    @Test
    void createRuleShouldReturnBudgetRule() throws Exception {
        BudgetRuleDTO rule = BudgetRuleDTO.builder()
                .id(71L)
                .budgetId(41L)
                .ruleType("threshold")
                .thresholdPercentage(new BigDecimal("80.00"))
                .enabled(true)
                .notificationTitle("Budget alert")
                .build();

        when(ledgerService.hasManagementPermission(31L, 1L)).thenReturn(true);
        when(budgetService.createRule(org.mockito.ArgumentMatchers.eq(31L), org.mockito.ArgumentMatchers.eq(41L), any()))
                .thenReturn(rule);

        mockMvc.perform(post("/api/ledgers/31/budgets/41/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "thresholdPercentage": 80,
                                  "enabled": true,
                                  "notificationTitle": "Budget alert",
                                  "notificationMessage": "Monthly budget is almost used up"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(71))
                .andExpect(jsonPath("$.budgetId").value(41))
                .andExpect(jsonPath("$.thresholdPercentage").value(80.00));

        verify(budgetService).createRule(org.mockito.ArgumentMatchers.eq(31L), org.mockito.ArgumentMatchers.eq(41L), any());
    }
}