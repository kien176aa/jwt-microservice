package com.javatechie.consumer;

import com.javatechie.client.ProductClient;
import com.javatechie.entity.Order;
import com.javatechie.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.example.dtos.OrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void processOrder(OrderDto order) {
        log.info("Processing order: " + order);
        CommonResponse<?> response = productClient.decreaseStock(order.getCartItems());
        log.info("decreaseStock: {}", response);
        if (response.getStatusCode() == HttpStatus.OK.value() && (Boolean) response.getData()) {
            order.setStatus("COMPLETED");
        } else {
            order.setStatus("FAILED");
        }
        orderRepository.save(new Order(order));
        log.info("Order status updated: " + order.getStatus());
    }
}
