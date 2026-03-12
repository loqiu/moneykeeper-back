package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.event.ExportJobEvent;

public interface ExportJobEventHandler {
    void handle(ExportJobEvent event);
}
