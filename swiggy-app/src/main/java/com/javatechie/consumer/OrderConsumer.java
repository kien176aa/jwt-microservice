package com.javatechie.consumer;

import com.javatechie.client.ProductClient;
import com.javatechie.entity.Order;
import com.javatechie.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.example.dtos.OrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void processOrder(OrderDto order) {
        log.info("Processing order: " + order);
        CommonResponse<?> response = productClient.decreaseStock(order.getCartItems());
        log.info("decreaseStock: {}", response);
        if (response.getStatusCode() == HttpStatus.OK.value() && StringUtils.EMPTY.equals(response.getData())) {
            order.setStatus("COMPLETED");
        } else {
            order.setStatus("FAILED");
        }
        orderRepository.save(new Order(order));
        log.info("Order status updated: " + order.getStatus());
        order.setStatus((String)response.getData());
        kafkaTemplate.send("notification-topic", order);
        log.info("Order sent to notification-service on topic 'notification-topic'");
    }
}
