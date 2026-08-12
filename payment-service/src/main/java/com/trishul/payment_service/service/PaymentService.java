package com.trishul.payment_service.service;

import com.trishul.payment_service.dto.PaymentResponse;
import com.trishul.payment_service.event.OrderCreatedEvent;
import com.trishul.payment_service.entity.Payment;
import com.trishul.payment_service.entity.PaymentStatus;
import com.trishul.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        BigDecimal amount = event.getPrice()
                .multiply(BigDecimal.valueOf(event.getQuantity()));

        payment.setAmount(amount);
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
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderNumber(String orderNumber) {

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for order number: " + orderNumber
                        )
                );

        return mapToPaymentResponse(payment);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getProcessedAt()
        );
    }

    private void validateEvent(OrderCreatedEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("Order created event must not be null");
        }

        if (event.getOrderNumber() == null ||
                event.getOrderNumber().isBlank()) {
            throw new IllegalArgumentException("Order number is required");
        }

        if (event.getQuantity() == null ||
                event.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        if (event.getPrice() == null || event.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

}
