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
    private String orderDate;
    private double totalPrice;
    private String status;
    private String transactionId;
    private List<CartItemDto> cartItems;
    private VoucherDto voucher;
}
