package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.dtos.CartItemDto;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long userId;
    private String name;
    private Integer quantity;
    private Double price;

    public CartItem(CartItemDto dto){
        productId = dto.getProductId();
        userId = dto.getUserId();
        name = dto.getName();
        quantity = dto.getQuantity();
        price = dto.getPrice();
    }
}

