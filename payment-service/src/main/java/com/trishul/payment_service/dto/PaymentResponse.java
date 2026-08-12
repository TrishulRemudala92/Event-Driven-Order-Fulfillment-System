package com.trishul.payment_service.dto;

import com.trishul.payment_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private String orderNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private LocalDateTime processedAt;
}