package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.event.RecordChangeType;
import com.loqiu.moneykeeper.event.RecordChangedEvent;
import com.loqiu.moneykeeper.service.BudgetService;
import com.loqiu.moneykeeper.service.RecordEventHandler;
import com.loqiu.moneykeeper.service.RecordSearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordEventHandlerImpl implements RecordEventHandler {

    private static final Logger logger = LogManager.getLogger(RecordEventHandlerImpl.class);

    @Autowired
    private RecordSearchService recordSearchService;

    @Autowired
    private BudgetService budgetService;

    @Override
    public void handle(RecordChangedEvent event) {
        if (event == null || event.getChangeType() == null) {
            return;
        }
        Long ledgerId = event.getLedgerId();
        MoneyKeeper previousRecord = event.getPreviousRecord();
        MoneyKeeper currentRecord = event.getCurrentRecord();
        Long recordId = resolveRecordId(event);

        if (RecordChangeType.DELETED.equals(event.getChangeType())) {
            recordSearchService.removeRecordIfEnabled(recordId);
            budgetService.syncThresholdNotificationsForLedgerRecord(ledgerId, previousRecord, null);
        } else {
            recordSearchService.syncRecordIfEnabled(recordId);
            budgetService.syncThresholdNotificationsForLedgerRecord(ledgerId, previousRecord, currentRecord);
        }

        logger.info("Handled record change event - eventId: {}, type: {}, recordId: {}",
                event.getEventId(), event.getChangeType(), recordId);
    }

    private Long resolveRecordId(RecordChangedEvent event) {
        if (event.getRecordId() != null) {
            return event.getRecordId();
        }
        if (event.getCurrentRecord() != null && event.getCurrentRecord().getId() != null) {
            return event.getCurrentRecord().getId();
        }
        if (event.getPreviousRecord() != null) {
            return event.getPreviousRecord().getId();
        }
        return null;
    }
}
