package com.example.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.OrderDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(OrderDto order) {
        log.info("Received order update from Kafka: {}", order);
        // Gửi message qua WebSocket đến client đăng ký vào /topic/order-status
        messagingTemplate.convertAndSend("/topic/order-status", order);
        log.info("Sent order update to WebSocket clients.");
    }
}
