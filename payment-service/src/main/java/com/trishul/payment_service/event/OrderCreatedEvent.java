package com.trishul.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrderCreatedEvent {

    private String eventId;
    private LocalDateTime occurredAt;
    private String orderNumber;
    private Long productId;
    private Integer quantity;
    private Double price;

}
