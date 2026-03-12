package com.loqiu.moneykeeper.service;

public interface ExportJobConsumerState {
    boolean isReady();

    void markReady();
}
