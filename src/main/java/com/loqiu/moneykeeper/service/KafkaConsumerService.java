package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.KafkaMessageRecord;

import java.util.List;

public interface KafkaConsumerService {
    List<KafkaMessageRecord> getRecentMessages(int limit);

    long getConsumedCount();

    boolean isEnabled();
}