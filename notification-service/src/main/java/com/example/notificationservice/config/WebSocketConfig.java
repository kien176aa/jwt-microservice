package com.example.notificationservice.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint cho client kết nối, sử dụng SockJS cho browser không hỗ trợ WebSocket
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Cho phép gửi tin nhắn đến các topic, ví dụ: /topic/order-status
        registry.enableSimpleBroker("/topic");
        // Tất cả các message gửi từ client có prefix /app sẽ được xử lý bởi các controller
        registry.setApplicationDestinationPrefixes("/app");
    }
}
