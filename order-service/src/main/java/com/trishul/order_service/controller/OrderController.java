package com.trishul.order_service.controller;

import com.trishul.order_service.dto.OrderRequest;
import com.trishul.order_service.dto.OrderResponse;
import com.trishul.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trishul.order_service.entity.OrderStatus;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestParam OrderStatus newStatus)
    {
        orderService.updateOrderStatus(orderNumber, newStatus);
        return ResponseEntity.noContent().build();
    }
}

