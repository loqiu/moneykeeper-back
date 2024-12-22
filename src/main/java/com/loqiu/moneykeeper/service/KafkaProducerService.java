package com.loqiu.moneykeeper.service;

public interface KafkaProducerService {

    public void sendMessage(String topic, String key, String value);
}
