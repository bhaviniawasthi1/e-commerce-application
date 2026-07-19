package com.ecommerce.service;

import com.ecommerce.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void simulatePayment_ReturnsValidStatus() {
        PaymentStatus status = paymentService.simulatePayment("UPI", 100.0);

        assertNotNull(status);
        assertTrue(status == PaymentStatus.SUCCESS
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.PENDING);
    }

    @Test
    void simulatePayment_MultipleCalls_ReturnsDifferentStatuses() {
        boolean seenSuccess = false;
        boolean seenFailed = false;
        boolean seenPending = false;

        for (int i = 0; i < 100; i++) {
            PaymentStatus status = paymentService.simulatePayment("CARD", 50.0);
            switch (status) {
                case SUCCESS -> seenSuccess = true;
                case FAILED -> seenFailed = true;
                case PENDING -> seenPending = true;
            }
        }

        assertTrue(seenSuccess, "Should see SUCCESS at least once");
        assertTrue(seenFailed, "Should see FAILED at least once");
        assertTrue(seenPending, "Should see PENDING at least once");
    }

    @Test
    void simulatePayment_CodMethod_ReturnsValidStatus() {
        PaymentStatus status = paymentService.simulatePayment("COD", 200.0);
        assertNotNull(status);
    }

    @Test
    void simulatePayment_NetBanking_ReturnsValidStatus() {
        PaymentStatus status = paymentService.simulatePayment("NET_BANKING", 150.0);
        assertNotNull(status);
    }

}
