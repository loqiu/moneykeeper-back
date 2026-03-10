package com.loqiu.moneykeeper.service;

public interface KafkaProducerService {

    void sendMessage(String topic, String key, String value);

    boolean isEnabled();
}