package com.trishul.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "order_number", nullable = false, unique = true)
        private String orderNumber;

        @Column(name = "product_id", nullable = false)
        private Long productId;

        @Column(nullable = false)
        private Integer quantity;

        @Column(nullable = false)
        private Double price;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private OrderStatus status;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;
    }

