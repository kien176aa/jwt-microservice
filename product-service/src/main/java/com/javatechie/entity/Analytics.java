package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "views")
    private Integer views;

    @Column(name = "clicks")
    private Integer clicks;

    @Column(name = "orders")
    private Integer orders;

    @Column(name = "revenue", precision = 10, scale = 2)
    private BigDecimal revenue;

    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate;

    @Column(name = "bounce_rate", precision = 5, scale = 4)
    private BigDecimal bounceRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}