package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.event.RecordChangedEvent;

public interface RecordEventHandler {
    void handle(RecordChangedEvent event);
}
