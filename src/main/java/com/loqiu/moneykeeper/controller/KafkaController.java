package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.KafkaMessageRecord;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.KafkaConsumerService;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/kafka")
@RestController
public class KafkaController {
    private static final Logger logger = LogManager.getLogger(KafkaController.class);

    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;

    @Autowired
    public KafkaController(KafkaProducerService producerService, KafkaConsumerService consumerService) {
        this.kafkaProducerService = producerService;
        this.kafkaConsumerService = consumerService;
    }

    @PostMapping("/send")
    public MkApiResponse<String> kafkaSendMessage(@RequestParam(required = false) String topic,
                                                  @RequestParam(required = false, defaultValue = "message") String key,
                                                  @RequestParam String message,
                                                  HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            return MkApiResponse.error(403, "Forbidden");
        }
        if (!StringUtils.hasText(message)) {
            return MkApiResponse.error(400, "Message cannot be empty");
        }
        logger.info("Sending message to Kafka");
        try {
            kafkaProducerService.sendMessage(topic, key, message.trim());
            return MkApiResponse.success("Message sent successfully");
        } catch (IllegalStateException e) {
            return MkApiResponse.error(503, e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send message - error: {}", e.getMessage());
            return MkApiResponse.error(500, "Failed to send message");
        }
    }

    @GetMapping("/listen")
    public MkApiResponse<String> kafkaListenMessage(@RequestParam(required = false) String topic,
                                                    @RequestParam(required = false, defaultValue = "message") String key,
                                                    @RequestParam String message,
                                                    HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            return MkApiResponse.error(403, "Forbidden");
        }
        if (!StringUtils.hasText(message)) {
            return MkApiResponse.error(400, "Message cannot be empty");
        }
        logger.info("Proxying Kafka listen test request");
        try {
            kafkaProducerService.sendMessage(topic, key, message.trim());
            return MkApiResponse.success("Message forwarded successfully");
        } catch (IllegalStateException e) {
            return MkApiResponse.error(503, e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to proxy listen request - error: {}", e.getMessage());
            return MkApiResponse.error(500, "Failed to process listen request");
        }
    }

    @GetMapping("/status")
    public MkApiResponse<Map<String, Object>> getKafkaStatus(HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            return MkApiResponse.error(403, "Forbidden");
        }
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", kafkaProducerService.isEnabled());
        status.put("consumerEnabled", kafkaConsumerService.isEnabled());
        status.put("consumedCount", kafkaConsumerService.getConsumedCount());
        status.put("implemented", true);
        return MkApiResponse.success(status);
    }

    @GetMapping("/messages")
    public MkApiResponse<List<KafkaMessageRecord>> getRecentMessages(@RequestParam(defaultValue = "20") int limit,
                                                                     HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            return MkApiResponse.error(403, "Forbidden");
        }
        if (limit < 1 || limit > 100) {
            return MkApiResponse.error(400, "Limit must be between 1 and 100");
        }
        return MkApiResponse.success(kafkaConsumerService.getRecentMessages(limit));
    }
}