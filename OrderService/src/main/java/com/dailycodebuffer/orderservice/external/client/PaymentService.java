package com.dailycodebuffer.orderservice.external.client;

import com.dailycodebuffer.orderservice.exception.OrderServiceException;
import com.dailycodebuffer.orderservice.external.request.PaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("PAYMENT-SERVICE/payment")
@CircuitBreaker(name = "external", fallbackMethod = "fallback")
public interface PaymentService {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    default ResponseEntity<Void> fallback(Exception e) {
        throw new OrderServiceException("Payment service is offline!", "PAYMENT_SERVICE_OFFLINE", 500);
    }

}
