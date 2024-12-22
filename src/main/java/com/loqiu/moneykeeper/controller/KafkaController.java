package com.loqiu.moneykeeper.controller;


import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/kafka")
@RestController
public class KafkaController {
    private static final Logger logger = LogManager.getLogger(KafkaController.class);

    private final KafkaProducerService kafkaProducerService;
    @Autowired
    public KafkaController(KafkaProducerService producerService) {
        this.kafkaProducerService = producerService;
    }

    @PostMapping("/send")
    public MkApiResponse<String> kafkaSendMessage(@RequestParam String message) {
        logger.info("Sending message to Kafka - message: {}", message);
        try{
            kafkaProducerService.sendMessage("quickstart-events", "message", message);
            return MkApiResponse.success("message send successfully");
        } catch (Exception e) {
            logger.error("Failed to send message - error: {}", e.getMessage());
            return MkApiResponse.error("Failed to send message");
        }
    }

    @GetMapping("/listen")
    public MkApiResponse<String> kafkaListenMessage(@RequestParam String message) {
        logger.info("Listening message to Kafka - message: {}", message);
        try{
            kafkaProducerService.sendMessage("quickstart-events", "message", message);
            return MkApiResponse.success("message Listen successfully");
        } catch (Exception e) {
            logger.error("Failed to Listen message - error: {}", e.getMessage());
            return MkApiResponse.error("Failed to Listen message");
        }
    }
}
