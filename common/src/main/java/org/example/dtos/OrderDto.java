package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private double totalPrice;
    private String status;
    private List<CartItemDto> cartItems;
}
