package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.service.ExportJobConsumerState;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExportJobConsumerStateImpl implements ExportJobConsumerState {

    private final AtomicBoolean ready = new AtomicBoolean(false);

    @Override
    public boolean isReady() {
        return ready.get();
    }

    @Override
    public void markReady() {
        ready.set(true);
    }
}
