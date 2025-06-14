package com.example.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.OrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(OrderDto order) {
        log.info("=== KAFKA MESSAGE RECEIVED ===");
        log.info("Order: {}", order);

        if (order.getUserId() == null) {
            log.error("UserId is null, cannot send notification");
            return;
        }

        String userId = order.getUserId().toString();
        log.info("Processing notification for userId: {}", userId);

        // Debug: Check active WebSocket sessions
        debugActiveSessions(userId);

        try {
            // Method 1: Send to specific user
            log.info("Sending to user: {} at destination: /topic/order-status", userId);
            messagingTemplate.convertAndSendToUser(userId, "queue/order-status", order);
            log.info("✓ Message sent to user successfully");

        } catch (Exception e) {
            log.error("✗ Error sending WebSocket message: {}", e.getMessage(), e);
        }

        log.info("=== KAFKA MESSAGE PROCESSING COMPLETED ===");
    }

    private void debugActiveSessions(String userId) {
        log.info("=== DEBUGGING WEBSOCKET SESSIONS ===");

        // Get all active users
        var users = userRegistry.getUsers();
        log.info("Total active WebSocket users: {}", users.size());

        for (SimpUser user : users) {
            log.info("Active user: {} with {} sessions", user.getName(), user.getSessions().size());
            if (userId.equals(user.getName())) {
                user.getSessions().forEach(session -> {
                    log.info("Session: {}", session);
                });
                log.info("✓ Target user {} found in active sessions!", userId);
                return;
            }
        }

        log.warn("✗ Target user {} NOT found in active sessions", userId);
    }
}
