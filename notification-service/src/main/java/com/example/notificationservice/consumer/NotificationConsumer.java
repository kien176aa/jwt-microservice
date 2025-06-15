package com.example.notificationservice.consumer;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.dtos.NotifyGrantVoucher;
import org.example.dtos.OrderDto;
import org.example.dtos.UserMessageDto;
import org.example.dtos.VoucherDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeOrderNotification(Object message) {
        log.info("Received order notification: " + message);
        if(message instanceof ConsumerRecord record) {
            if(record.value() instanceof OrderDto orderDto) {
                processSendNotifyOrder(orderDto);
            } else if (record.value() instanceof NotifyGrantVoucher notify) {
                for (UserMessageDto userMessageDto : notify.getMessages()) {
                    Notification notification = new Notification();
                    notification.setUserId(userMessageDto.getUserId());
                    notification.setMessage(userMessageDto.getMessage());
                    notification.setTitle(notify.getTitle());
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                }
            }
        }


    }

    private void processSendNotifyOrder(OrderDto order) {
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
