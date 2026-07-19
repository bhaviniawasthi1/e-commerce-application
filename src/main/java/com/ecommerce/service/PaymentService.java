package com.ecommerce.service;

import com.ecommerce.entity.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class PaymentService {

    private static final String[] STATUSES = {"SUCCESS", "FAILED", "PENDING"};
    private static final int[] WEIGHTS = {60, 25, 15};
    private final Random random = new Random();

    public PaymentStatus simulatePayment(String paymentMethod, double amount) {
        log.info("Simulating payment: method={}, amount={}", paymentMethod, amount);

        String result = getWeightedRandom();
        PaymentStatus status = PaymentStatus.valueOf(result);

        log.info("Payment simulation result: {}", status);
        return status;
    }

    private String getWeightedRandom() {
        int totalWeight = 0;
        for (int w : WEIGHTS) {
            totalWeight += w;
        }

        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < STATUSES.length; i++) {
            cumulativeWeight += WEIGHTS[i];
            if (randomValue < cumulativeWeight) {
                return STATUSES[i];
            }
        }

        return STATUSES[0];
    }

}
