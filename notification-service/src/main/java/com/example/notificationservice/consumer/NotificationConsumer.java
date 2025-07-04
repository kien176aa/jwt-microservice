package com.example.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.OrderDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(OrderDto order) {
        log.info("Received order update from Kafka: {}", order);
        
        try {
            // Gửi message qua Redis pub/sub để tất cả pods nhận được
            String orderJson = objectMapper.writeValueAsString(order);
            redisTemplate.convertAndSend("websocket:notifications", orderJson);
            
            log.info("Published order update to Redis channel");
        } catch (Exception e) {
            log.error("Error publishing to Redis: ", e);
        }
    }
}
