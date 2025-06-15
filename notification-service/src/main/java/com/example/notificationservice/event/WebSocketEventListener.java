package com.example.notificationservice.event;

import com.example.notificationservice.client.IdentityClient;
import com.example.notificationservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.dtos.NotificationDto;
import org.example.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Slf4j
public class WebSocketEventListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private IdentityClient identityClient;


    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        log.info("Received a new session connected event1: " + headerAccessor);
        // Truy xuất connect message
        Message<?> connectMessage = (Message<?>) headerAccessor.getHeader("simpConnectMessage");
        String authToken = null;
        if (connectMessage != null) {
            StompHeaderAccessor connectAccessor = StompHeaderAccessor.wrap(connectMessage);
            authToken = connectAccessor.getFirstNativeHeader("Authorization");
        } else {
            log.info("No simpConnectMessage found.");
            return;
        }
        log.info("Received a new session connected event2: " + authToken);
        if (authToken != null && authToken.startsWith("Bearer ")) {
            try {
                String userId = validateTokenAndGetUserId(authToken);

                if(userId == null) {
                    log.error("Invalid token - no user found!!!!");
                    return;
                }

                log.info("User {} connected to WebSocket", userId);

                // Get unread notifications for user
                List<NotificationDto> notifications = notificationService.getAllNotifications(Long.parseLong(userId));

                // Send notifications to user
                if (!notifications.isEmpty()) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notifications);
                            log.info("Sent {} notifications to user {}", notifications.size(), userId);
                        } catch (InterruptedException e) {
                            log.error("WebSocketEventListener - InterruptedException: {}", e.getMessage());
                        }
                    }).start();

                }

            } catch (Exception e) {
                log.error("Error processing WebSocket connection: ", e);
            }
        }
    }

    private String validateTokenAndGetUserId(String token) {
        try {
            String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
            log.info("WebSocketEventListener - Validating WebSocket token");

            CommonResponse<?> authResponse = identityClient.getCurrentUser(authHeader);
            log.info("WebSocketEventListener - Auth response: {}", authResponse);

            if (authResponse == null ||
                    authResponse.getStatusCode() != HttpStatus.OK.value() ||
                    authResponse.getData() == null) {
                log.error("WebSocketEventListener - Invalid auth response");
                return null;
            }

            Long userId = null;
            Object data = authResponse.getData();

            if (data instanceof UserDto) {
                userId = ((UserDto) data).getId();
            } else if (data instanceof LinkedHashMap) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> userMap = (LinkedHashMap<String, Object>) data;
                Object idObj = userMap.get("id");
                if (idObj instanceof Number) {
                    userId = ((Number) idObj).longValue();
                }
            }

            log.info("WebSocketEventListener - Extracted userId: {}", userId);
            return userId != null ? userId.toString() : null;

        } catch (Exception e) {
            log.error("WebSocketEventListener - WebSocket token validation error: {}", e.getMessage(), e);
            return null;
        }
    }
}
