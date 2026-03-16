package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.RecordEventDispatcher;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerRecordControllerTest {

    @Mock
    private MoneyKeeperService moneyKeeperService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private RecordEventDispatcher recordEventDispatcher;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LedgerRecordController controller = new LedgerRecordController();
        ReflectionTestUtils.setField(controller, "moneyKeeperService", moneyKeeperService);
        ReflectionTestUtils.setField(controller, "categoryService", categoryService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(controller, "recordEventDispatcher", recordEventDispatcher);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createRecordShouldUseLedgerScopedCategory() throws Exception {
        Category category = new Category();
        category.setId(8L);
        category.setLedgerId(31L);
        category.setUserId(1L);
        category.setType("expense");

        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(categoryService.getById(8L)).thenReturn(category);
        when(moneyKeeperService.insertMoneyKeeper(any(MoneyKeeper.class))).thenAnswer(invocation -> {
            MoneyKeeper record = invocation.getArgument(0);
            record.setId(11L);
            return true;
        });

        mockMvc.perform(post("/api/ledgers/31/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "userId": 99,
                                  "categoryId": 8,
                                  "type": "expense",
                                  "amount": 18.50,
                                  "transactionDate": "2026-03-11",
                                  "notes": "Team lunch"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.ledgerId").value(31))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.categoryId").value(8));

        ArgumentCaptor<MoneyKeeper> recordCaptor = ArgumentCaptor.forClass(MoneyKeeper.class);
        verify(recordEventDispatcher).dispatchRecordCreated(eq(31L), recordCaptor.capture());
        assertEquals(11L, recordCaptor.getValue().getId());
        assertEquals(31L, recordCaptor.getValue().getLedgerId());
        assertEquals(2L, recordCaptor.getValue().getUserId());
    }

    @Test
    void createRecordShouldRejectChineseType() throws Exception {
        Category category = new Category();
        category.setId(8L);
        category.setLedgerId(31L);
        category.setUserId(1L);
        category.setType("expense");

        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(categoryService.getById(8L)).thenReturn(category);

        mockMvc.perform(post("/api/ledgers/31/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "categoryId": 8,
                                  "type": "支出",
                                  "amount": 18.50,
                                  "transactionDate": "2026-03-11",
                                  "notes": "Team lunch"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Record type must be income or expense"));
    }

    @Test
    void updateRecordShouldRejectForeignMember() throws Exception {
        MoneyKeeper existingRecord = new MoneyKeeper();
        existingRecord.setId(11L);
        existingRecord.setLedgerId(31L);
        existingRecord.setUserId(1L);
        existingRecord.setCategoryId(8L);
        existingRecord.setType("expense");
        existingRecord.setAmount(new BigDecimal("18.50"));
        existingRecord.setTransactionDate(LocalDate.of(2026, 3, 11));

        when(moneyKeeperService.getById(11L)).thenReturn(existingRecord);
        when(ledgerService.hasManagementPermission(31L, 2L)).thenReturn(false);
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);

        mockMvc.perform(put("/api/ledgers/31/records/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "amount": 20.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to modify this record"));
    }

    @Test
    void updateRecordShouldSyncBudgetThresholdNotifications() throws Exception {
        Category category = new Category();
        category.setId(8L);
        category.setLedgerId(31L);
        category.setType("expense");

        MoneyKeeper existingRecord = new MoneyKeeper();
        existingRecord.setId(11L);
        existingRecord.setLedgerId(31L);
        existingRecord.setUserId(2L);
        existingRecord.setCategoryId(8L);
        existingRecord.setType("expense");
        existingRecord.setAmount(new BigDecimal("18.50"));
        existingRecord.setTransactionDate(LocalDate.of(2026, 3, 11));

        MoneyKeeper storedRecord = new MoneyKeeper();
        storedRecord.setId(11L);
        storedRecord.setLedgerId(31L);
        storedRecord.setUserId(2L);
        storedRecord.setCategoryId(8L);
        storedRecord.setType("expense");
        storedRecord.setAmount(new BigDecimal("20.00"));
        storedRecord.setTransactionDate(LocalDate.of(2026, 3, 11));

        when(moneyKeeperService.getById(11L)).thenReturn(existingRecord, storedRecord);
        when(categoryService.getById(8L)).thenReturn(category);
        when(ledgerService.hasManagementPermission(31L, 2L)).thenReturn(false);
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);

        mockMvc.perform(put("/api/ledgers/31/records/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "amount": 20.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(20.00));

        ArgumentCaptor<MoneyKeeper> previousCaptor = ArgumentCaptor.forClass(MoneyKeeper.class);
        ArgumentCaptor<MoneyKeeper> currentCaptor = ArgumentCaptor.forClass(MoneyKeeper.class);
        verify(recordEventDispatcher).dispatchRecordUpdated(eq(31L), previousCaptor.capture(), currentCaptor.capture());
        assertEquals(new BigDecimal("18.50"), previousCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("20.00"), currentCaptor.getValue().getAmount());
    }

    @Test
    void deleteRecordShouldSyncBudgetThresholdNotifications() throws Exception {
        MoneyKeeper existingRecord = new MoneyKeeper();
        existingRecord.setId(11L);
        existingRecord.setLedgerId(31L);
        existingRecord.setUserId(2L);
        existingRecord.setCategoryId(8L);
        existingRecord.setType("expense");
        existingRecord.setAmount(new BigDecimal("18.50"));
        existingRecord.setTransactionDate(LocalDate.of(2026, 3, 11));

        when(moneyKeeperService.getById(11L)).thenReturn(existingRecord);
        when(ledgerService.hasManagementPermission(31L, 2L)).thenReturn(false);
        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);

        mockMvc.perform(delete("/api/ledgers/31/records/11")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk());

        ArgumentCaptor<MoneyKeeper> previousCaptor = ArgumentCaptor.forClass(MoneyKeeper.class);
        verify(recordEventDispatcher).dispatchRecordDeleted(eq(31L), previousCaptor.capture());
        assertEquals(11L, previousCaptor.getValue().getId());
        assertNull(previousCaptor.getValue().getDeletedAt());
    }

    @Test
    void listRecordsShouldReturnLedgerRecords() throws Exception {
        MoneyKeeper record = new MoneyKeeper();
        record.setId(11L);
        record.setLedgerId(31L);
        record.setUserId(1L);
        record.setCategoryId(8L);
        record.setType("expense");
        record.setAmount(new BigDecimal("18.50"));
        record.setTransactionDate(LocalDate.of(2026, 3, 11));

        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(moneyKeeperService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<MoneyKeeper>>any())).thenReturn(List.of(record));

        mockMvc.perform(get("/api/ledgers/31/records")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].ledgerId").value(31));
    }

    @Test
    void getSummaryShouldAggregateLedgerRecords() throws Exception {
        MoneyKeeper income = new MoneyKeeper();
        income.setType("income");
        income.setAmount(new BigDecimal("100.00"));

        MoneyKeeper expense = new MoneyKeeper();
        expense.setType("expense");
        expense.setAmount(new BigDecimal("30.00"));

        when(ledgerService.hasActiveMembership(31L, 2L)).thenReturn(true);
        when(moneyKeeperService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<MoneyKeeper>>any())).thenReturn(List.of(income, expense));

        mockMvc.perform(get("/api/ledgers/31/records/summary")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 2L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(100.00))
                .andExpect(jsonPath("$.totalExpense").value(30.00))
                .andExpect(jsonPath("$.balance").value(70.00));
    }
}
