package com.trishul.order_service.dto;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


public class OrderRequestValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }

    @Test
    void shouldAcceptValidOrderRequest() {
        OrderRequest request = new OrderRequest(
                101L,
                2,
                49.99
        );

        Set<ConstraintViolation<OrderRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectMissingProductId() {
        OrderRequest request = new OrderRequest(
                null,
                2,
                49.99
        );

        Set<ConstraintViolation<OrderRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Product ID is required"))
        );
    }

    @Test
    void shouldRejectQuantityBelowOne() {
        OrderRequest request = new OrderRequest(
                101L,
                0,
                49.99
        );

        Set<ConstraintViolation<OrderRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Quantity must be at least 1"))
        );
    }

    @Test
    void shouldRejectPriceBelowMinimum() {
        OrderRequest request = new OrderRequest(
                101L,
                2,
                0.0
        );

        Set<ConstraintViolation<OrderRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Price must be greater than 0"))
        );
    }
}
