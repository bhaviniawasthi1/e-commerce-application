package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    long countByUserId(Long userId);

    long count();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = :status")
    BigDecimal sumTotalAmountByPaymentStatus(@Param("status") PaymentStatus status);

    @Query(value = "SELECT oi.product_id, p.name, p.image_url, SUM(oi.quantity) as total_qty, SUM(oi.subtotal) as total_revenue " +
                   "FROM order_items oi " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.payment_status = 'SUCCESS' " +
                   "GROUP BY oi.product_id, p.name, p.image_url " +
                   "ORDER BY total_qty DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopSellingProducts(@Param("limit") int limit);

}
