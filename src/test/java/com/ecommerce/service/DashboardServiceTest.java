package com.ecommerce.service;

import com.ecommerce.dto.DashboardResponse;
import com.ecommerce.dto.TopSellingProductResponse;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.entity.Role;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboard_ReturnsAllMetrics() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(8L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);
        when(productRepository.countByIsActiveTrue()).thenReturn(50L);
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.sumTotalAmountByPaymentStatus(PaymentStatus.SUCCESS))
                .thenReturn(new BigDecimal("50000.00"));
        when(productRepository.findByStockLessThanEqualAndIsActiveTrue(10))
                .thenReturn(List.of());
        when(orderRepository.findTopSellingProducts(10))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(10L, response.getTotalUsers());
        assertEquals(8L, response.getTotalCustomers());
        assertEquals(2L, response.getTotalAdmins());
        assertEquals(50L, response.getTotalProducts());
        assertEquals(100L, response.getTotalOrders());
        assertEquals(new BigDecimal("50000.00"), response.getTotalRevenue());
        assertEquals(0L, response.getLowStockProducts());
        assertTrue(response.getTopSellingProducts().isEmpty());
    }

    @Test
    void getDashboard_WithLowStockProducts() {
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(4L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        when(productRepository.countByIsActiveTrue()).thenReturn(20L);
        when(orderRepository.count()).thenReturn(30L);
        when(orderRepository.sumTotalAmountByPaymentStatus(PaymentStatus.SUCCESS))
                .thenReturn(BigDecimal.ZERO);
        when(productRepository.findByStockLessThanEqualAndIsActiveTrue(10))
                .thenReturn(List.of(mock(com.ecommerce.entity.Product.class)));
        when(orderRepository.findTopSellingProducts(10))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(1L, response.getLowStockProducts());
    }

    @Test
    void getDashboard_WithTopSellingProducts() {
        Object[] row1 = {1L, "Product A", "img.jpg", 50L, new BigDecimal("25000.00")};
        Object[] row2 = {2L, "Product B", null, 30L, new BigDecimal("15000.00")};

        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(4L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        when(productRepository.countByIsActiveTrue()).thenReturn(20L);
        when(orderRepository.count()).thenReturn(30L);
        when(orderRepository.sumTotalAmountByPaymentStatus(PaymentStatus.SUCCESS))
                .thenReturn(new BigDecimal("40000.00"));
        when(productRepository.findByStockLessThanEqualAndIsActiveTrue(10))
                .thenReturn(List.of());
        when(orderRepository.findTopSellingProducts(10))
                .thenReturn(List.of(row1, row2));

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(2, response.getTopSellingProducts().size());
        assertEquals("Product A", response.getTopSellingProducts().get(0).getProductName());
        assertEquals(50L, response.getTopSellingProducts().get(0).getTotalQuantitySold());
        assertEquals("Product B", response.getTopSellingProducts().get(1).getProductName());
    }

}
