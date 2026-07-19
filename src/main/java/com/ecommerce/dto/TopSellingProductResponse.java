package com.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductResponse {

    private Long productId;
    private String productName;
    private String productImage;
    private long totalQuantitySold;
    private BigDecimal totalRevenue;

}
