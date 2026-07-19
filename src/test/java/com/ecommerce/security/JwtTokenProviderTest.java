package com.ecommerce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "2a6b3f7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5";
        jwtTokenProvider = new JwtTokenProvider(secret, 86400000L);
    }

    @Test
    void generateToken_Success() {
        String token = jwtTokenProvider.generateToken("testuser", "CUSTOMER");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getUsernameFromToken_ReturnsCorrectUsername() {
        String token = jwtTokenProvider.generateToken("testuser", "CUSTOMER");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken("testuser", "CUSTOMER");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void generateToken_DifferentUsers_ProduceDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken("user1", "CUSTOMER");
        String token2 = jwtTokenProvider.generateToken("user2", "ADMIN");

        assertNotEquals(token1, token2);
    }

}
