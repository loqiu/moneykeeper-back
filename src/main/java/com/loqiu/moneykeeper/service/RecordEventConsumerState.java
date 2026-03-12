package com.loqiu.moneykeeper.service;

public interface RecordEventConsumerState {
    boolean isReady();

    void markReady();
}
