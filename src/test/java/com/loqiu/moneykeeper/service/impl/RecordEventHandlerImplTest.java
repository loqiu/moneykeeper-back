package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.event.RecordChangedEvent;
import com.loqiu.moneykeeper.service.BudgetService;
import com.loqiu.moneykeeper.service.RecordSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecordEventHandlerImplTest {

    @Mock
    private RecordSearchService recordSearchService;

    @Mock
    private BudgetService budgetService;

    private RecordEventHandlerImpl recordEventHandler;

    @BeforeEach
    void setUp() {
        recordEventHandler = new RecordEventHandlerImpl();
        ReflectionTestUtils.setField(recordEventHandler, "recordSearchService", recordSearchService);
        ReflectionTestUtils.setField(recordEventHandler, "budgetService", budgetService);
    }

    @Test
    void handleCreatedEventShouldSyncSearchAndBudgetThresholds() {
        MoneyKeeper currentRecord = buildRecord(11L);

        recordEventHandler.handle(RecordChangedEvent.created(31L, currentRecord));

        verify(recordSearchService).syncRecordIfEnabled(11L);
        verify(budgetService).syncThresholdNotificationsForLedgerRecord(31L, null, currentRecord);
    }

    @Test
    void handleDeletedEventShouldRemoveSearchDocumentAndSyncBudgetThresholds() {
        MoneyKeeper previousRecord = buildRecord(11L);

        recordEventHandler.handle(RecordChangedEvent.deleted(31L, previousRecord));

        verify(recordSearchService).removeRecordIfEnabled(11L);
        verify(budgetService).syncThresholdNotificationsForLedgerRecord(31L, previousRecord, null);
    }

    private MoneyKeeper buildRecord(Long id) {
        MoneyKeeper record = new MoneyKeeper();
        record.setId(id);
        record.setLedgerId(31L);
        record.setUserId(2L);
        record.setCategoryId(8L);
        record.setType("expense");
        record.setAmount(new BigDecimal("18.50"));
        record.setTransactionDate(LocalDate.of(2026, 3, 12));
        return record;
    }
}
