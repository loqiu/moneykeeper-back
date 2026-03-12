package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.entity.MoneyKeeper;

public interface RecordEventDispatcher {
    void dispatchRecordCreated(Long ledgerId, MoneyKeeper currentRecord);

    void dispatchRecordUpdated(Long ledgerId, MoneyKeeper previousRecord, MoneyKeeper currentRecord);

    void dispatchRecordDeleted(Long ledgerId, MoneyKeeper previousRecord);
}
