package com.ecommerce.service;

import com.ecommerce.dto.PagedResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        productRequest = ProductRequest.builder()
                .name("Smartphone")
                .description("Latest smartphone")
                .categoryId(1L)
                .price(new BigDecimal("699.99"))
                .stock(50)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Smartphone")
                .description("Latest smartphone")
                .category(category)
                .price(new BigDecimal("699.99"))
                .stock(50)
                .status(ProductStatus.ACTIVE)
                .isActive(true)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals("Smartphone", response.getName());
        assertEquals(new BigDecimal("699.99"), response.getPrice());
        assertEquals(50, response.getStock());
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productRequest));
    }

    @Test
    void updateProduct_Success() {
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Phone")
                .description("Updated desc")
                .categoryId(1L)
                .price(new BigDecimal("799.99"))
                .stock(30)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertEquals("Updated Phone", response.getName());
        assertEquals(new BigDecimal("799.99"), response.getPrice());
        assertEquals(30, response.getStock());
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, productRequest));
    }

    @Test
    void deleteProduct_SoftDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(999L));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals("Smartphone", response.getName());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void browseProducts_Success() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.filterProducts(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<ProductResponse> response = productService.browseProducts(
                null, null, null, null, 0, 10, "createdAt", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getAllProductsForAdmin_Success() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> response =
                productService.getAllProductsForAdmin(0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

}
