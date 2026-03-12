package com.loqiu.moneykeeper.service;

public interface ExportJobEventDispatcher {
    void dispatchCreated(Long jobId, Long ledgerId, Long requestedByUserId);
}
