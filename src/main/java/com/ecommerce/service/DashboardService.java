package com.ecommerce.service;

import com.ecommerce.dto.DashboardResponse;
import com.ecommerce.dto.TopSellingProductResponse;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.entity.Role;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;

    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long totalProducts = productRepository.countByIsActiveTrue();
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByPaymentStatus(PaymentStatus.SUCCESS);
        long lowStockProducts = productRepository
                .findByStockLessThanEqualAndIsActiveTrue(LOW_STOCK_THRESHOLD).size();
        List<TopSellingProductResponse> topSellingProducts = getTopSellingProducts(10);

        log.info("Admin dashboard fetched: users={}, products={}, orders={}, revenue={}",
                totalUsers, totalProducts, totalOrders, totalRevenue);

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalAdmins(totalAdmins)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .lowStockProducts(lowStockProducts)
                .topSellingProducts(topSellingProducts)
                .build();
    }

    private List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        List<Object[]> results = orderRepository.findTopSellingProducts(limit);

        return results.stream()
                .map(row -> TopSellingProductResponse.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .productImage((String) row[2])
                        .totalQuantitySold(((Number) row[3]).longValue())
                        .totalRevenue((BigDecimal) row[4])
                        .build())
                .toList();
    }

}
