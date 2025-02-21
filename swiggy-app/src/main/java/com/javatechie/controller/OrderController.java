package com.javatechie.controller;

import com.javatechie.client.ProductClient;
import com.javatechie.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.example.dtos.OrderDto;
import org.example.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final ProductClient productClient;
    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    @PostMapping("/checkout")
    public CommonResponse<String> checkout(@RequestParam Long userId) {
        log.info("start checkout: {}", userId);
        CommonResponse<List<CartItemDto>> response = productClient.getProductByUserId(userId);
        log.info("get product by user: {}", response);
        if (response.getStatusCode() != HttpStatus.OK.value() || response.getData().isEmpty()) {
            return CommonResponse.notOk("Cart is empty");
        }

        double totalPrice = 0;
        for (CartItemDto item : response.getData()) {
            totalPrice += item.getPrice() * item.getQuantity();
        }

        OrderDto order = new OrderDto(null, userId, LocalDateTime.now(), totalPrice, "PENDING", response.getData());

        kafkaTemplate.send("order-topic", order);

        return CommonResponse.ok("Order placed successfully, processing...");
    }
}

