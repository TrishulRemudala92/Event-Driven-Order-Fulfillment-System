package com.trishul.order_service.service;

import com.trishul.order_service.dto.OrderRequest;
import com.trishul.order_service.dto.OrderResponse;
import com.trishul.order_service.entity.Order;
import com.trishul.order_service.entity.OrderStatus;
import com.trishul.order_service.event.OrderCreatedEvent;
import com.trishul.order_service.kafka.OrderEventProducer;
import com.trishul.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                orderEventProducer
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        OrderRequest request = new OrderRequest(
                101L,
                2,
                49.99
        );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getOrderNumber());
        assertEquals("CREATED", response.getStatus());

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertNotNull(savedOrder.getOrderNumber());
        assertEquals(101L, savedOrder.getProductId());
        assertEquals(2, savedOrder.getQuantity());
        assertEquals(49.99, savedOrder.getPrice(), 0.001);
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());

        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedEvent.class);

        verify(orderEventProducer)
                .sendOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent event = eventCaptor.getValue();

        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredAt());
        assertEquals(savedOrder.getOrderNumber(), event.getOrderNumber());
        assertEquals(savedOrder.getProductId(), event.getProductId());
        assertEquals(savedOrder.getQuantity(), event.getQuantity());
        assertEquals(savedOrder.getPrice(), event.getPrice());
    }

    @Test
    void shouldReturnAllOrders() {
        // Arrange
        Order firstOrder = createOrder(
                1L,
                "ORDER-1001",
                101L,
                2,
                49.99,
                OrderStatus.CREATED
        );

        Order secondOrder = createOrder(
                2L,
                "ORDER-1002",
                102L,
                1,
                79.99,
                OrderStatus.CONFIRMED
        );

        when(orderRepository.findAll())
                .thenReturn(List.of(firstOrder, secondOrder));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertEquals(2, responses.size());

        assertEquals("ORDER-1001", responses.get(0).getOrderNumber());
        assertEquals("CREATED", responses.get(0).getStatus());

        assertEquals("ORDER-1002", responses.get(1).getOrderNumber());
        assertEquals("CONFIRMED", responses.get(1).getStatus());

        verify(orderRepository).findAll();
        verifyNoInteractions(orderEventProducer);
    }

    @Test
    void shouldReturnEmptyListWhenNoOrdersExist() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(List.of());

        // Act
        List<OrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(orderRepository).findAll();
    }

    @Test
    void shouldReturnOrderByOrderNumber() {
        // Arrange
        Order order = createOrder(
                1L,
                "ORDER-1001",
                101L,
                2,
                49.99,
                OrderStatus.CREATED
        );

        when(orderRepository.findByOrderNumber("ORDER-1001"))
                .thenReturn(Optional.of(order));

        // Act
        OrderResponse response =
                orderService.getOrderByOrderNumber("ORDER-1001");

        // Assert
        assertEquals("ORDER-1001", response.getOrderNumber());
        assertEquals("CREATED", response.getStatus());

        verify(orderRepository)
                .findByOrderNumber("ORDER-1001");
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        // Arrange
        when(orderRepository.findByOrderNumber("ORDER-9999"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.getOrderByOrderNumber("ORDER-9999")
        );

        // Assert
        assertEquals(
                "Order not found with order number: ORDER-9999",
                exception.getMessage()
        );

        verify(orderRepository)
                .findByOrderNumber("ORDER-9999");
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        // Arrange
        Order order = createOrder(
                1L,
                "ORDER-1001",
                101L,
                2,
                49.99,
                OrderStatus.CREATED
        );

        when(orderRepository.findByOrderNumber("ORDER-1001"))
                .thenReturn(Optional.of(order));

        // Act
        orderService.updateOrderStatus(
                "ORDER-1001",
                OrderStatus.CONFIRMED
        );

        // Assert
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        verify(orderRepository)
                .findByOrderNumber("ORDER-1001");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingOrder() {
        // Arrange
        when(orderRepository.findByOrderNumber("ORDER-9999"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.updateOrderStatus(
                        "ORDER-9999",
                        OrderStatus.CONFIRMED
                )
        );

        // Assert
        assertEquals(
                "Order not found with order number: ORDER-9999",
                exception.getMessage()
        );

        verify(orderRepository)
                .findByOrderNumber("ORDER-9999");

        verify(orderRepository, never()).save(any(Order.class));
    }

    private Order createOrder(
            Long id,
            String orderNumber,
            Long productId,
            Integer quantity,
            Double price,
            OrderStatus status
    ) {
        Order order = new Order();

        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());

        return order;
    }
}
