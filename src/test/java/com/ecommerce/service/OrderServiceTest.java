package com.ecommerce.service;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PagedResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private CartItem cartItem;
    private OrderRequest orderRequest;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .role(Role.CUSTOMER)
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

        orderRequest = OrderRequest.builder()
                .paymentMethod("UPI")
                .shippingAddress("123 Test St, City")
                .build();

        orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("699.99"))
                .subtotal(new BigDecimal("1399.98"))
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-20260718-0001")
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("1399.98"))
                .paymentMethod("UPI")
                .paymentStatus(PaymentStatus.SUCCESS)
                .shippingAddress("123 Test St, City")
                .orderItems(List.of(orderItem))
                .build();
    }

    @Test
    void placeOrder_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(paymentService.simulatePayment(anyString(), anyDouble()))
                .thenReturn(PaymentStatus.SUCCESS);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.placeOrder("testuser", orderRequest);

        assertNotNull(response);
        assertEquals("ORD-20260718-0001", response.getOrderNumber());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals("SUCCESS", response.getPaymentStatus());
        assertEquals(new BigDecimal("1399.98"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());

        verify(cartItemRepository).deleteByUserId(1L);
        verify(productRepository).save(product);
        assertEquals(48, product.getStock());
    }

    @Test
    void placeOrder_EmptyCart_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder("testuser", orderRequest));
    }

    @Test
    void placeOrder_InvalidPaymentMethod_ThrowsException() {
        OrderRequest badRequest = OrderRequest.builder()
                .paymentMethod("BITCOIN")
                .shippingAddress("123 Test St")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder("testuser", badRequest));
    }

    @Test
    void placeOrder_PaymentFailed_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(paymentService.simulatePayment(anyString(), anyDouble()))
                .thenReturn(PaymentStatus.FAILED);

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder("testuser", orderRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_PaymentPending_CreatesOrder() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(paymentService.simulatePayment(anyString(), anyDouble()))
                .thenReturn(PaymentStatus.PENDING);

        Order pendingOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-20260718-0001")
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1399.98"))
                .paymentMethod("UPI")
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress("123 Test St, City")
                .orderItems(List.of(orderItem))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        OrderResponse response = orderService.placeOrder("testuser", orderRequest);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals("PENDING", response.getPaymentStatus());
    }

    @Test
    void getUserOrders_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<OrderResponse> response =
                orderService.getUserOrders("testuser", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        OrderResponse response = orderService.getOrderById(1L, "testuser");

        assertNotNull(response);
        assertEquals("ORD-20260718-0001", response.getOrderNumber());
    }

    @Test
    void getOrderById_Unauthorized_ThrowsException() {
        User otherUser = User.builder().id(2L).username("other").role(Role.CUSTOMER).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThrows(BadRequestException.class,
                () -> orderService.getOrderById(1L, "other"));
    }

    @Test
    void cancelOrder_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(1L, "testuser");

        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(productRepository).save(product);
        assertEquals(52, product.getStock());
    }

    @Test
    void cancelOrder_AlreadyShipped_ThrowsException() {
        order.setStatus(OrderStatus.SHIPPED);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(1L, "testuser"));
    }

    @Test
    void updateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.updateOrderStatus(1L, "SHIPPED");

        assertEquals("SHIPPED", response.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidStatus_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(1L, "INVALID"));
    }

    @Test
    void getOrderByNumber_Success() {
        when(orderRepository.findByOrderNumber("ORD-20260718-0001")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderByNumber("ORD-20260718-0001");

        assertNotNull(response);
    }

    @Test
    void getAllOrders_Success() {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<OrderResponse> response = orderService.getAllOrders(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

}
