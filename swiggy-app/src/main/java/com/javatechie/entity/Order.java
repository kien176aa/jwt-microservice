package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dtos.OrderDto;

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
    private String cartItemsJson;

    public Order(OrderDto dto){
        userId = dto.getUserId();
        orderDate = dto.getOrderDate();
        status = dto.getStatus();
        cartItemsJson = dto.getCartItems().toString();
    }
}

