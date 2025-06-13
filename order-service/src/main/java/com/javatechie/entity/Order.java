package com.javatechie.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dtos.OrderDto;
import org.example.utils.JsonUtil;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private double totalPrice;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String cartItemsJson;

    public Order(OrderDto dto){
        userId = dto.getUserId();
        orderDate = dto.getOrderDate();
        status = dto.getStatus();
        totalPrice = dto.getTotalPrice();
        try {
            cartItemsJson = JsonUtil.toJson(dto.getCartItems());
        } catch (JsonProcessingException e) {
            cartItemsJson = "";
        }
    }
}

