package com.javatechie.consumer;

import com.javatechie.client.ProductClient;
import com.javatechie.entity.Order;
import com.javatechie.repository.OrderRepository;
import com.javatechie.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.dtos.CommonResponse;
import org.example.dtos.OrderDto;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-topic", groupId = "order-group", containerFactory = "genericKafkaListenerContainerFactory")
    @Transactional
    public void processOrder(Object mess) {
        log.info("Order processed: " + mess);
        try {
            if (mess instanceof ConsumerRecord record) {
                if (record.value() instanceof OrderDto order) {
                    log.info("Processing order: " + order);
                    CommonResponse<?> response = productClient.decreaseStock(order.getCartItems());
                    log.info("decreaseStock: {}", response);
                    if (response.getStatusCode() == HttpStatus.OK.value() && "".equals(response.getData())) {
                        order.setStatus("COMPLETED");
                        applyVoucher(order);
                    } else {
                        order.setStatus("FAILED");
                    }
                    orderRepository.save(new Order(order));
                    log.info("Order status updated: " + order.getStatus());
                    order.setStatus((String) response.getData());
                    kafkaTemplate.send("notification-topic", order);
                    log.info("Order sent to notification-service on topic 'notification-topic'");
                } else {
                    log.error("Invalid mess: {}", mess);
                }
            }
        } catch (Exception e) {
            log.error("Err - OrderConsumer: {}", e.getMessage());
        }
    }

    private void applyVoucher(OrderDto order) {
        log.info("Applying voucher: {}", order.getVoucher());
        if(order.getVoucher() == null || order.getVoucher().getId() == null) {
            log.info("No voucher");
            return;
        }
        double discount = order.getVoucher().getDiscountAmount();
        if(order.getVoucher().isPercent()){
            discount = order.getVoucher().getDiscountAmount() * order.getTotalPrice() / 100;
        }
        double newPrice = Math.max(0, order.getTotalPrice() - discount);
        BigDecimal roundedPrice = BigDecimal.valueOf(newPrice).setScale(2, RoundingMode.HALF_UP);
        order.setTotalPrice(roundedPrice.doubleValue());
        voucherRepository.updateStatus(order.getVoucher().getId());
        log.info("Applying voucher end: {}", order.getVoucher());
    }
}
