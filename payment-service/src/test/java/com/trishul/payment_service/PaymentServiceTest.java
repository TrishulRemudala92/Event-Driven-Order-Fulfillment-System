package com.trishul.payment_service;

import com.trishul.payment_service.entity.Payment;
import com.trishul.payment_service.entity.PaymentStatus;
import com.trishul.payment_service.event.OrderCreatedEvent;
import com.trishul.payment_service.repository.PaymentRepository;
import com.trishul.payment_service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private OrderCreatedEvent orderCreatedEvent;

    @BeforeEach
    void setUp() {

        orderCreatedEvent = new OrderCreatedEvent();

        orderCreatedEvent.setOrderNumber("ORD-1001");
        orderCreatedEvent.setQuantity(2);
        orderCreatedEvent.setPrice(49.99);
    }

    @Test
    void processOrderCreatedEvent_shouldCreateAndSavePayment() {

        when(paymentRepository.findByOrderNumber("ORD-1001"))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setId(1L);
                    return payment;
                });

        Payment result =
                paymentService.processOrderCreatedEvent(orderCreatedEvent);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ORD-1001", result.getOrderNumber());
        assertEquals(99.98, result.getAmount(), 0.001);
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals("NOT_SELECTED", result.getPaymentMethod());
        assertNotNull(result.getProcessedAt());

        verify(paymentRepository)
                .findByOrderNumber("ORD-1001");

        verify(paymentRepository)
                .save(any(Payment.class));
    }

    @Test
    void processOrderCreatedEvent_shouldSaveCorrectPaymentDetails() {

        when(paymentRepository.findByOrderNumber("ORD-1001"))
                .thenReturn(Optional.empty());

        ArgumentCaptor<Payment> paymentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.processOrderCreatedEvent(orderCreatedEvent);

        verify(paymentRepository).save(paymentCaptor.capture());

        Payment capturedPayment = paymentCaptor.getValue();

        assertEquals("ORD-1001", capturedPayment.getOrderNumber());
        assertEquals(99.98, capturedPayment.getAmount(), 0.001);
        assertEquals(PaymentStatus.PENDING, capturedPayment.getStatus());
        assertEquals(
                "NOT_SELECTED",
                capturedPayment.getPaymentMethod()
        );
        assertNotNull(capturedPayment.getProcessedAt());
    }

    @Test
    void processOrderCreatedEvent_shouldReturnExistingPaymentWhenDuplicateEventReceived() {

        Payment existingPayment = new Payment(
                1L,
                "ORD-1001",
                99.98,
                PaymentStatus.PENDING,
                "NOT_SELECTED",
                LocalDateTime.now()
        );

        when(paymentRepository.findByOrderNumber("ORD-1001"))
                .thenReturn(Optional.of(existingPayment));

        Payment result =
                paymentService.processOrderCreatedEvent(orderCreatedEvent);

        assertNotNull(result);
        assertSame(existingPayment, result);
        assertEquals("ORD-1001", result.getOrderNumber());

        verify(paymentRepository)
                .findByOrderNumber("ORD-1001");

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenEventIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(null)
                );

        assertEquals(
                "Order created event must not be null",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenOrderNumberIsNull() {

        orderCreatedEvent.setOrderNumber(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(
                                orderCreatedEvent
                        )
                );

        assertEquals(
                "Order number is required",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenOrderNumberIsBlank() {

        orderCreatedEvent.setOrderNumber("   ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(
                                orderCreatedEvent
                        )
                );

        assertEquals(
                "Order number is required",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenQuantityIsNull() {

        orderCreatedEvent.setQuantity(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(
                                orderCreatedEvent
                        )
                );

        assertEquals(
                "Quantity must be at least 1",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenQuantityIsZero() {

        orderCreatedEvent.setQuantity(0);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(
                                orderCreatedEvent
                        )
                );

        assertEquals(
                "Quantity must be at least 1",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenPriceIsNull() {

        orderCreatedEvent.setPrice(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(
                                orderCreatedEvent
                        )
                );

        assertEquals(
                "Price must be greater than 0",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processOrderCreatedEvent_shouldThrowExceptionWhenPriceIsZero() {

        orderCreatedEvent.setPrice(0.0);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> paymentService.processOrderCreatedEvent(
                                orderCreatedEvent
                        )
                );

        assertEquals(
                "Price must be greater than 0",
                exception.getMessage()
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void getPaymentByOrderNumber_shouldReturnPaymentWhenFound() {

        Payment payment = new Payment(
                1L,
                "ORD-1001",
                99.98,
                PaymentStatus.PENDING,
                "NOT_SELECTED",
                LocalDateTime.now()
        );

        when(paymentRepository.findByOrderNumber("ORD-1001"))
                .thenReturn(Optional.of(payment));

        Payment result =
                paymentService.getPaymentByOrderNumber("ORD-1001");

        assertNotNull(result);
        assertEquals("ORD-1001", result.getOrderNumber());
        assertEquals(99.98, result.getAmount(), 0.001);

        verify(paymentRepository)
                .findByOrderNumber("ORD-1001");
    }

    @Test
    void getPaymentByOrderNumber_shouldThrowExceptionWhenPaymentNotFound() {

        when(paymentRepository.findByOrderNumber("ORD-9999"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService
                                .getPaymentByOrderNumber("ORD-9999")
                );

        assertEquals(
                "Payment not found for order: ORD-9999",
                exception.getMessage()
        );

        verify(paymentRepository)
                .findByOrderNumber("ORD-9999");
    }
}
