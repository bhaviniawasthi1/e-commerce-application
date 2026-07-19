package com.ecommerce.service;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private CartItem cartItem;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Smartphone")
                .price(new BigDecimal("699.99"))
                .stock(50)
                .status(ProductStatus.ACTIVE)
                .isActive(true)
                .build();

        cartItem = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(2)
                .build();

        cartItemRequest = CartItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();
    }

    @Test
    void getCart_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse response = cartService.getCart("testuser");

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        assertEquals(new BigDecimal("1399.98"), response.getTotalPrice());
    }

    @Test
    void addItem_NewItem_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse response = cartService.addItem("testuser", cartItemRequest);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
    }

    @Test
    void addItem_ExistingItem_MergesQuantity() {
        CartItem existing = CartItem.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(1)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existing);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(existing));

        CartResponse response = cartService.addItem("testuser", cartItemRequest);

        assertNotNull(response);
        assertEquals(3, existing.getQuantity());
    }

    @Test
    void addItem_ProductNotAvailable_ThrowsException() {
        product.setStatus(ProductStatus.INACTIVE);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class,
                () -> cartService.addItem("testuser", cartItemRequest));
    }

    @Test
    void addItem_InsufficientStock_ThrowsException() {
        product.setStock(1);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class,
                () -> cartService.addItem("testuser", cartItemRequest));
    }

    @Test
    void updateItemQuantity_Increase_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse response = cartService.updateItemQuantity("testuser", 1L, 5);

        assertNotNull(response);
        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void updateItemQuantity_RemoveWhenZero() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        CartResponse response = cartService.updateItemQuantity("testuser", 1L, 0);

        assertNotNull(response);
        assertEquals(0, response.getItems().size());
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeItem_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        CartResponse response = cartService.removeItem("testuser", 1L);

        assertNotNull(response);
        assertEquals(0, response.getItems().size());
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeItem_NotFound_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdAndProductId(eq(1L), anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeItem("testuser", 999L));
    }

    @Test
    void clearCart_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        cartService.clearCart("testuser");

        verify(cartItemRepository).deleteByUserId(1L);
    }

}
