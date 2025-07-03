package com.example.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dtos.OrderDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageListener implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            OrderDto order = objectMapper.readValue(messageBody, OrderDto.class);
            
            // Gửi đến tất cả WebSocket clients connected với pod này
            messagingTemplate.convertAndSend("/topic/order-status", order);
            
            log.info("Sent order update to WebSocket clients: {}", order);
        } catch (Exception e) {
            log.error("Error processing Redis message: ", e);
        }
    }
}