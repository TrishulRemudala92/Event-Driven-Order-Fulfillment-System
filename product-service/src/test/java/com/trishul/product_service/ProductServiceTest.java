package com.trishul.product_service;

import com.trishul.product_service.dto.ProductRequest;
import com.trishul.product_service.dto.ProductResponse;
import com.trishul.product_service.entity.Product;
import com.trishul.product_service.repository.ProductRepository;
import com.trishul.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {


    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldSaveAndReturnProductResponse() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Laptop",
                "Gaming laptop",
                999.99,
                10
        );

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    product.setId(1L);
                    return product;
                });

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("Gaming laptop", response.getDescription());
        assertEquals(999.99, response.getPrice());
        assertEquals(10, response.getStockQuantity());
        assertTrue(response.isActive());

        ArgumentCaptor<Product> productCaptor =
                ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        assertEquals("Laptop", capturedProduct.getName());
        assertEquals("Gaming laptop", capturedProduct.getDescription());
        assertEquals(999.99, capturedProduct.getPrice());
        assertEquals(10, capturedProduct.getStockQuantity());
        assertTrue(capturedProduct.getActive());
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {

        // Arrange
        Product productOne = new Product(
                1L,
                "Laptop",
                "Gaming laptop",
                999.99,
                10,
                true
        );

        Product productTwo = new Product(
                2L,
                "Keyboard",
                "Mechanical keyboard",
                79.99,
                20,
                true
        );

        when(productRepository.findAll())
                .thenReturn(List.of(productOne, productTwo));

        // Act
        List<ProductResponse> responses =
                productService.getAllProducts();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals("Laptop", responses.get(0).getName());
        assertEquals(999.99, responses.get(0).getPrice());

        assertEquals(2L, responses.get(1).getId());
        assertEquals("Keyboard", responses.get(1).getName());
        assertEquals(79.99, responses.get(1).getPrice());

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_shouldReturnEmptyListWhenNoProductsExist() {

        // Arrange
        when(productRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<ProductResponse> responses =
                productService.getAllProducts();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(productRepository).findAll();
    }

    @Test
    void getProductById_shouldReturnProductWhenProductExists() {

        // Arrange
        Product product = new Product(
                1L,
                "Laptop",
                "Gaming laptop",
                999.99,
                10,
                true
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        // Act
        ProductResponse response =
                productService.getProductById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("Gaming laptop", response.getDescription());
        assertEquals(999.99, response.getPrice());
        assertEquals(10, response.getStockQuantity());
        assertTrue(response.isActive());

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.getProductById(99L)
        );

        // Assert
        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository).findById(99L);
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProductResponse() {

        // Arrange
        Product existingProduct = new Product(
                1L,
                "Old Laptop",
                "Old description",
                699.99,
                5,
                true
        );

        ProductRequest updateRequest = new ProductRequest(
                "Updated Laptop",
                "Updated description",
                899.99,
                15
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        // Act
        ProductResponse response =
                productService.updateProduct(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Updated Laptop", response.getName());
        assertEquals("Updated description", response.getDescription());
        assertEquals(899.99, response.getPrice());
        assertEquals(15, response.getStockQuantity());
        assertTrue(response.isActive());

        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        ProductRequest updateRequest = new ProductRequest(
                "Updated Laptop",
                "Updated description",
                899.99,
                15
        );

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.updateProduct(99L, updateRequest)
        );

        // Assert
        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteProductWhenProductExists() {

        // Arrange
        Product product = new Product(
                1L,
                "Laptop",
                "Gaming laptop",
                999.99,
                10,
                true
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.deleteProduct(99L)
        );

        // Assert
        assertEquals(
                "Product not found with id: 99",
                exception.getMessage()
        );

        verify(productRepository).findById(99L);
        verify(productRepository, never()).delete(any(Product.class));
    }
}