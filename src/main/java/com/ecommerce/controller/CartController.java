package com.ecommerce.controller;

import com.ecommerce.constants.AppConstants;
import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.BASE_URL + "/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse response = cartService.getCart(userDetails.getUsername());
        return ResponseEntity
                .ok(ApiResponse.success("Cart fetched successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(userDetails.getUsername(), request);
        return ResponseEntity
                .ok(ApiResponse.success("Item added to cart", response));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam @Valid int quantity) {
        CartResponse response = cartService.updateItemQuantity(
                userDetails.getUsername(), productId, quantity);
        return ResponseEntity
                .ok(ApiResponse.success("Cart item updated", response));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        CartResponse response = cartService.removeItem(userDetails.getUsername(), productId);
        return ResponseEntity
                .ok(ApiResponse.success("Item removed from cart", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity
                .ok(ApiResponse.success("Cart cleared successfully", null));
    }

}
