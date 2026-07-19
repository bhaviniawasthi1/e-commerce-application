package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getCart(String username) {
        User user = getUser(username);
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(cartItems);
    }

    @Transactional
    public CartResponse addItem(String username, CartItemRequest request) {
        User user = getUser(username);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isActive() || product.getStatus().name().equals("INACTIVE")) {
            throw new BadRequestException("Product is not available");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException(
                    "Insufficient stock. Available: " + product.getStock());
        }

        cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + request.getQuantity();
                            if (newQty > product.getStock()) {
                                throw new BadRequestException(
                                        "Insufficient stock. Available: " + product.getStock());
                            }
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                            log.debug("Updated cart item quantity for product {}: {}",
                                    product.getId(), newQty);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .user(user)
                                    .product(product)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartItemRepository.save(newItem);
                            log.debug("Added new cart item for product {}", product.getId());
                        });

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(cartItems);
    }

    @Transactional
    public CartResponse updateItemQuantity(String username, Long productId, int quantity) {
        User user = getUser(username);
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        Product product = cartItem.getProduct();

        if (quantity > product.getStock()) {
            throw new BadRequestException(
                    "Insufficient stock. Available: " + product.getStock());
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.debug("Removed cart item for product {} due to quantity <= 0", productId);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            log.debug("Updated cart item quantity for product {} to {}", productId, quantity);
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(cartItems);
    }

    @Transactional
    public CartResponse removeItem(String username, Long productId) {
        User user = getUser(username);
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));
        cartItemRepository.delete(cartItem);
        log.info("Removed product {} from cart", productId);

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(cartItems);
    }

    @Transactional
    public void clearCart(String username) {
        User user = getUser(username);
        cartItemRepository.deleteByUserId(user.getId());
        log.info("Cart cleared for user: {}", username);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private CartResponse buildCartResponse(List<CartItem> cartItems) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToItemResponse)
                .toList();

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImageUrl())
                .unitPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

}
