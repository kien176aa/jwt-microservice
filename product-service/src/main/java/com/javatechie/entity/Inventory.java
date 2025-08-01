package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    @Column(name = "incoming_quantity")
    private Integer incomingQuantity;

    @Column(name = "committed_quantity")
    private Integer committedQuantity;

    @Column(name = "damaged_quantity")
    private Integer damagedQuantity;

    @Column(name = "quality_control_quantity")
    private Integer qualityControlQuantity;

    @Column(name = "safety_stock_quantity")
    private Integer safetyStockQuantity;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}