package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.entity.Category;
import com.loqiu.moneykeeper.exception.GlobalExceptionHandler;
import com.loqiu.moneykeeper.service.CategoryService;
import com.loqiu.moneykeeper.service.LedgerService;
import com.loqiu.moneykeeper.service.MoneyKeeperService;
import com.loqiu.moneykeeper.service.RecordSearchService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MoneyKeeperControllerTest {

    @Mock
    private MoneyKeeperService moneyKeeperService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private RecordSearchService recordSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MoneyKeeperController controller = new MoneyKeeperController();
        ReflectionTestUtils.setField(controller, "moneyKeeperService", moneyKeeperService);
        ReflectionTestUtils.setField(controller, "categoryService", categoryService);
        ReflectionTestUtils.setField(controller, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(controller, "recordSearchService", recordSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createRecordShouldUseCurrentUserIdForNormalUsers() throws Exception {
        Category category = new Category();
        category.setId(5L);
        category.setUserId(1L);
        category.setLedgerId(21L);
        category.setType("expense");

        when(categoryService.getById(5L)).thenReturn(category);
        when(moneyKeeperService.insertMoneyKeeper(any(com.loqiu.moneykeeper.entity.MoneyKeeper.class))).thenAnswer(invocation -> {
            com.loqiu.moneykeeper.entity.MoneyKeeper record = invocation.getArgument(0);
            record.setId(10L);
            return true;
        });

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "userId": 99,
                                  "categoryId": 5,
                                  "type": "expense",
                                  "amount": 88.50,
                                  "transactionDate": "2026-03-08",
                                  "notes": "Lunch"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.ledgerId").value(21))
                .andExpect(jsonPath("$.categoryId").value(5))
                .andExpect(jsonPath("$.type").value("expense"));

        ArgumentCaptor<com.loqiu.moneykeeper.entity.MoneyKeeper> captor = ArgumentCaptor.forClass(com.loqiu.moneykeeper.entity.MoneyKeeper.class);
        verify(moneyKeeperService).insertMoneyKeeper(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(21L, captor.getValue().getLedgerId());
        verify(recordSearchService).syncRecordIfEnabled(10L);
    }

    @Test
    void createRecordShouldRejectMismatchedCategoryType() throws Exception {
        Category category = new Category();
        category.setId(5L);
        category.setUserId(1L);
        category.setLedgerId(21L);
        category.setType("income");

        when(categoryService.getById(5L)).thenReturn(category);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "categoryId": 5,
                                  "type": "expense",
                                  "amount": 88.50,
                                  "transactionDate": "2026-03-08"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Record type must match the selected category type"))
                .andExpect(jsonPath("$.path").value("/api/records"));
    }

    @Test
    void updateRecordShouldSyncOnlyThatRecord() throws Exception {
        com.loqiu.moneykeeper.entity.MoneyKeeper existingRecord = new com.loqiu.moneykeeper.entity.MoneyKeeper();
        existingRecord.setId(10L);
        existingRecord.setUserId(1L);
        existingRecord.setLedgerId(21L);
        existingRecord.setCategoryId(5L);
        existingRecord.setType("expense");
        existingRecord.setAmount(new BigDecimal("88.50"));
        existingRecord.setTransactionDate(LocalDate.of(2026, 3, 8));
        existingRecord.setNotes("Lunch");

        com.loqiu.moneykeeper.entity.MoneyKeeper refreshedRecord = new com.loqiu.moneykeeper.entity.MoneyKeeper();
        refreshedRecord.setId(10L);
        refreshedRecord.setUserId(1L);
        refreshedRecord.setLedgerId(21L);
        refreshedRecord.setCategoryId(5L);
        refreshedRecord.setType("expense");
        refreshedRecord.setAmount(new BigDecimal("99.00"));
        refreshedRecord.setTransactionDate(LocalDate.of(2026, 3, 9));
        refreshedRecord.setNotes("Dinner");

        Category category = new Category();
        category.setId(5L);
        category.setUserId(1L);
        category.setLedgerId(21L);
        category.setType("expense");

        when(moneyKeeperService.getById(10L)).thenReturn(existingRecord, refreshedRecord);
        when(categoryService.getById(5L)).thenReturn(category);
        when(moneyKeeperService.updateById(any(com.loqiu.moneykeeper.entity.MoneyKeeper.class))).thenReturn(true);

        mockMvc.perform(put("/api/records/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user")
                        .content("""
                                {
                                  "amount": 99.00,
                                  "transactionDate": "2026-03-09",
                                  "notes": "Dinner"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(99.00));

        verify(recordSearchService).syncRecordIfEnabled(10L);
    }

    @Test
    void deleteRecordShouldRemoveOnlyThatRecordFromIndex() throws Exception {
        com.loqiu.moneykeeper.entity.MoneyKeeper existingRecord = new com.loqiu.moneykeeper.entity.MoneyKeeper();
        existingRecord.setId(10L);
        existingRecord.setUserId(1L);
        existingRecord.setLedgerId(21L);
        existingRecord.setCategoryId(5L);
        existingRecord.setType("expense");
        existingRecord.setAmount(new BigDecimal("88.50"));
        existingRecord.setTransactionDate(LocalDate.of(2026, 3, 8));

        when(moneyKeeperService.getById(10L)).thenReturn(existingRecord);
        when(moneyKeeperService.update(any())).thenReturn(true);

        mockMvc.perform(delete("/api/records/10")
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ID, 1L)
                        .requestAttr(RequestAuthUtil.CURRENT_USER_ROLE, "user"))
                .andExpect(status().isOk());

        verify(recordSearchService).removeRecordIfEnabled(10L);
    }
}
