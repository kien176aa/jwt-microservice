package com.example.notificationservice.config;

import com.example.notificationservice.client.IdentityClient;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private IdentityClient identityClient;

    // Store session to user mapping for debugging
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        log.info("WebSocket message - Command: {}, Session: {}",
                accessor.getCommand(), accessor.getSessionId());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleConnect(message, accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return handleSubscribe(message, accessor);
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            return handleDisconnect(message, accessor);
        }

        return message;
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        try {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders == null || authHeaders.isEmpty()) {
                log.error("No Authorization header in WebSocket connect");
                return null; // Reject connection
            }

            String token = authHeaders.get(0);
            log.info("WebSocket connecting with token: {}", token);

            String userId = validateTokenAndGetUserId(token);
            if (userId == null) {
                log.error("Invalid token for WebSocket connection");
                return null; // Reject connection
            }

            // Create user principal and set it
            UserPrincipal userPrincipal = new UserPrincipal(userId);
            accessor.setUser(userPrincipal);

            // Store session mapping
            String sessionId = accessor.getSessionId();
            sessionUserMap.put(sessionId, userId);

            log.info("WebSocket connected successfully - User: {}, Session: {}", userId, sessionId);
            return message;

        } catch (Exception e) {
            log.error("WebSocket connect error: {}", e.getMessage(), e);
            return null; // Reject connection
        }
    }

    private Message<?> handleSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String userId = sessionUserMap.get(sessionId);

        log.info("WebSocket subscribe - Session: {}, User: {}, Destination: {}",
                sessionId, userId, destination);

        return message;
    }

    private Message<?> handleDisconnect(Message<?> message, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String userId = sessionUserMap.remove(sessionId);

        log.info("WebSocket disconnected - Session: {}, User: {}", sessionId, userId);
        return message;
    }

    private String validateTokenAndGetUserId(String token) {
        try {
            String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
            log.info("Validating WebSocket token");

            CommonResponse<?> authResponse = identityClient.getCurrentUser(authHeader);
            log.info("Auth response: {}", authResponse);

            if (authResponse == null ||
                    authResponse.getStatusCode() != HttpStatus.OK.value() ||
                    authResponse.getData() == null) {
                log.error("Invalid auth response");
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

            log.info("Extracted userId: {}", userId);
            return userId != null ? userId.toString() : null;

        } catch (Exception e) {
            log.error("WebSocket token validation error: {}", e.getMessage(), e);
            return null;
        }
    }

    // Inner class for User Principal
    public static class UserPrincipal implements Principal {
        private final String userId;

        public UserPrincipal(String userId) {
            this.userId = userId;
        }

        @Override
        public String getName() {
            return userId;
        }

        public String getUserId() {
            return userId;
        }
    }
}