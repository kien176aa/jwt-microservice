package com.example.notificationservice.consumer;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.RedisWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.dtos.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final RedisWebSocketService redisWebSocketService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    @Transactional
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

        try {
            // Use Redis service to send to user across all pods
            redisWebSocketService.sendToUser(userId, "/queue/order-status", order);
            log.info("✓ Message sent to user via Redis service");

            if (order.getVoucher() != null && order.getVoucher().getId() != null) {
                notificationRepository.updateStatusNotify(order.getVoucher().getId());
            }

        } catch (Exception e) {
            log.error("✗ Error sending WebSocket message: {}", e.getMessage(), e);
        }

        try{
            if(order.getCartItems() != null && !order.getCartItems().isEmpty()
            && ("COMPLETED".equals(order.getStatus()) || "".equals(order.getStatus()))) {
                log.info("Update quantity sent to topic via Redis service");
                order.getCartItems().forEach(item -> item.setUserId(null));
                updateQuantityRealTime(order.getCartItems());
            }
        } catch (Exception e) {
            log.error("Error update quantity realtime: {}", e.getMessage());
        }

        log.info("=== KAFKA MESSAGE PROCESSING COMPLETED ===");
    }

    private void updateQuantityRealTime(List<CartItemDto> cartItems) {
        log.info("Update quantity realtime start: {}", cartItems);
        redisWebSocketService.sendToTopic("/topic/product-quantity", cartItems);
        log.info("Update quantity realtime end: {}", cartItems);
    }
}