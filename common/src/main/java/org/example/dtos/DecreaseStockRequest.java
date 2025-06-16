package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecreaseStockRequest {
    private List<CartItemDto> cartItems;
    private String transactionId;
}
