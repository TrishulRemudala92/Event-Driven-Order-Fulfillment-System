package com.trishul.order_service.service;

import com.trishul.order_service.dto.OrderRequest;
import com.trishul.order_service.dto.OrderResponse;
import com.trishul.order_service.entity.Order;
import com.trishul.order_service.entity.OrderStatus;
import com.trishul.order_service.event.OrderCreatedEvent;
import com.trishul.order_service.kafka.OrderEventProducer;
import com.trishul.order_service.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        Order order = new Order();

        order.setOrderNumber(UUID.randomUUID().toString());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                savedOrder.getOrderNumber(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getPrice()
        );

        orderEventProducer.sendOrderCreatedEvent(event);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with order number: " + orderNumber
                        )
                );

        return mapToResponse(order);
    }

    @Transactional
    public void updateOrderStatus(String orderNumber,  OrderStatus newStatus) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with order number: " + orderNumber
                        )
                );

        order.setStatus(newStatus);
    }

    private OrderResponse mapToResponse(Order order) {

        return new OrderResponse(
                order.getOrderNumber(),
                order.getStatus().name()
        );
    }
}

