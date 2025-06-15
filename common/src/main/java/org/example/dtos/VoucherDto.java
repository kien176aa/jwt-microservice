package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherDto {
    private Long id;
    private Long userId;
    private String code;
    private Double discountAmount;
    private Double minPurchase;
    private boolean isPercent;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private boolean used;
}
