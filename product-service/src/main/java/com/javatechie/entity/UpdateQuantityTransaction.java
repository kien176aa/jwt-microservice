package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "update_quantity_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateQuantityTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;
    private Long userId;
    private Long productId;
    private String transactionId;
}

