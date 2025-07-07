package com.example.notificationservice.handler;

import com.example.notificationservice.service.RedisWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageHandler {

    private final RedisWebSocketService redisWebSocketService;

    public void onRedisMessage(String message) {
        redisWebSocketService.processRedisMessage(message);
    }
}
