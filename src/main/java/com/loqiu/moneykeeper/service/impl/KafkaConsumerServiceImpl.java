package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.config.KafkaFeatureProperties;
import com.loqiu.moneykeeper.constant.KafkaTopicConstant;
import com.loqiu.moneykeeper.dto.KafkaMessageRecord;
import com.loqiu.moneykeeper.service.KafkaConsumerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {
    private static final Logger logger = LogManager.getLogger(KafkaConsumerServiceImpl.class);
    private static final int MAX_RECENT_MESSAGES = 100;

    private final Deque<KafkaMessageRecord> recentMessages = new ConcurrentLinkedDeque<>();
    private final AtomicLong consumedCount = new AtomicLong(0);

    @Autowired
    private KafkaFeatureProperties kafkaFeatureProperties;

    @KafkaListener(topics = KafkaTopicConstant.QUICKSTART_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            KafkaMessageRecord messageRecord = KafkaMessageRecord.builder()
                    .topic(record.topic())
                    .key(record.key())
                    .value(record.value())
                    .receivedAt(LocalDateTime.now())
                    .build();
            recentMessages.addFirst(messageRecord);
            while (recentMessages.size() > MAX_RECENT_MESSAGES) {
                recentMessages.removeLast();
            }
            consumedCount.incrementAndGet();
            logger.info("Received message from topic: {}, key: {}, value: {}", record.topic(), record.key(), record.value());
        } catch (Exception e) {
            logger.error("Error processing Kafka message", e);
        }
    }

    @Override
    public List<KafkaMessageRecord> getRecentMessages(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, MAX_RECENT_MESSAGES));
        List<KafkaMessageRecord> results = new ArrayList<>(resolvedLimit);
        int current = 0;
        for (KafkaMessageRecord recentMessage : recentMessages) {
            results.add(recentMessage);
            current++;
            if (current >= resolvedLimit) {
                break;
            }
        }
        return results;
    }

    @Override
    public long getConsumedCount() {
        return consumedCount.get();
    }

    @Override
    public boolean isEnabled() {
        return kafkaFeatureProperties.isEnabled();
    }
}
