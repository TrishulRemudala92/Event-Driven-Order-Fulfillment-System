package com.trishul.payment_service.service;

import com.trishul.payment_service.event.OrderCreatedEvent;
import com.trishul.payment_service.entity.Payment;
import com.trishul.payment_service.entity.PaymentStatus;
import com.trishul.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processOrderCreatedEvent(OrderCreatedEvent event) {

        validateEvent(event);

        Optional<Payment> existingPayment =
                paymentRepository.findByOrderNumber(event.getOrderNumber());

        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Payment payment = new Payment();

        payment.setOrderNumber(event.getOrderNumber());
        payment.setAmount(event.getQuantity() * event.getPrice());
        payment.setStatus(PaymentStatus.PENDING);

        /*
         * The OrderCreatedEvent currently does not contain a payment method.
         * This is only a temporary value for the learning project.
         */
        payment.setPaymentMethod("NOT_SELECTED");
        payment.setProcessedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderNumber(String orderNumber) {

        return paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for order: " + orderNumber
                        )
                );
    }

    private void validateEvent(OrderCreatedEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Order created event must not be null"
            );
        }

        if (event.getOrderNumber() == null ||
                event.getOrderNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Order number is required"
            );
        }

        if (event.getQuantity() == null ||
                event.getQuantity() < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }

        if (event.getPrice() == null ||
                event.getPrice() <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than 0"
            );
        }
    }

}
