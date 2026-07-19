package com.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalUsers;
    private long totalCustomers;
    private long totalAdmins;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long lowStockProducts;
    private List<TopSellingProductResponse> topSellingProducts;

}
