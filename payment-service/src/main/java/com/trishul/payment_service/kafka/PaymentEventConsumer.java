package com.trishul.payment_service.kafka;

import com.trishul.payment_service.event.OrderCreatedEvent;
import com.trishul.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${app.kafka.order-created-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {

        paymentService.processOrderCreatedEvent(event);
    }

}
