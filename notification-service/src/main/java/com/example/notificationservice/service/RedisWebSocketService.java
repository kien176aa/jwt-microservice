package com.example.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisWebSocketService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpUserRegistry userRegistry;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String WEBSOCKET_CHANNEL = "websocket-notifications";
    private static final String USER_SESSION_KEY = "websocket:user:sessions:";

    @PostConstruct
    public void init() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(this, "onRedisMessage");
        redisMessageListenerContainer.addMessageListener(adapter, new ChannelTopic(WEBSOCKET_CHANNEL));
    }

    public void sendToUser(String userId, String destination, Object message) {
        try {
            log.info("Sending message to user: {} at destination: {}", userId, destination);

            // Check if user is connected to this pod
            boolean userConnectedLocally = isUserConnectedLocally(userId);

            if (userConnectedLocally) {
                // Send directly to local user
                messagingTemplate.convertAndSendToUser(userId, destination, message);
                log.info("Message sent to local user: {}", userId);
            }

            // Always publish to Redis for other pods (don't check if user is local)
            RedisMessage redisMessage = new RedisMessage(userId, destination, message, "USER");
            redisTemplate.convertAndSend(WEBSOCKET_CHANNEL, redisMessage);
            log.info("Message published to Redis for user: {}", userId);

        } catch (Exception e) {
            log.error("Error sending message to user {}: {}", userId, e.getMessage(), e);
        }
    }

    public void sendToTopic(String destination, Object message) {
        try {
            // Send to local topic subscribers
            messagingTemplate.convertAndSend(destination, message);

            // Publish to Redis for other pods
            RedisMessage redisMessage = new RedisMessage(null, destination, message, "TOPIC");
            redisTemplate.convertAndSend(WEBSOCKET_CHANNEL, redisMessage);

        } catch (Exception e) {
            log.error("Error sending message to topic {}: {}", destination, e.getMessage(), e);
        }
    }

    public void onRedisMessage(String message) {
        try {
            RedisMessage redisMessage = objectMapper.readValue(message, RedisMessage.class);
            log.info("Received Redis message: {}", redisMessage);

            if ("USER".equals(redisMessage.getMessageType())) {
                // Check if user is connected to this pod before forwarding
                if (isUserConnectedLocally(redisMessage.getUserId())) {
                    messagingTemplate.convertAndSendToUser(
                            redisMessage.getUserId(),
                            redisMessage.getDestination(),
                            redisMessage.getMessage()
                    );
                    log.info("Forwarded Redis message to local user: {}", redisMessage.getUserId());
                }
            } else if ("TOPIC".equals(redisMessage.getMessageType())) {
                messagingTemplate.convertAndSend(redisMessage.getDestination(), redisMessage.getMessage());
                log.info("Forwarded Redis message to local topic: {}", redisMessage.getDestination());
            }

        } catch (Exception e) {
            log.error("Error processing Redis message: {}", e.getMessage(), e);
        }
    }

    private boolean isUserConnectedLocally(String userId) {
        if (userId == null) return false;

        for (SimpUser user : userRegistry.getUsers()) {
            if (userId.equals(user.getName())) {
                return !user.getSessions().isEmpty();
            }
        }
        return false;
    }

    // Track user sessions in Redis for debugging
    public void trackUserSession(String userId, String sessionId, boolean connect) {
        try {
            String key = USER_SESSION_KEY + userId;
            if (connect) {
                redisTemplate.opsForSet().add(key, sessionId);
                redisTemplate.expire(key, 1, TimeUnit.HOURS);
            } else {
                redisTemplate.opsForSet().remove(key, sessionId);
            }
        } catch (Exception e) {
            log.error("Error tracking user session: {}", e.getMessage());
        }
    }

    public static class RedisMessage {
        private String userId;
        private String destination;
        private Object message;
        private String messageType; // USER or TOPIC

        public RedisMessage() {}

        public RedisMessage(String userId, String destination, Object message, String messageType) {
            this.userId = userId;
            this.destination = destination;
            this.message = message;
            this.messageType = messageType;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public Object getMessage() { return message; }
        public void setMessage(Object message) { this.message = message; }
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
    }
}