package com.dailycodebuffer.orderservice.external.client;

import com.dailycodebuffer.orderservice.exception.OrderServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("PRODUCT-SERVICE/product")
@CircuitBreaker(name = "external", fallbackMethod = "fallback")
public interface ProductService {

    @PutMapping("/reduceQuantity/{id}")
    ResponseEntity<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity);

    default ResponseEntity<Void> fallback(Exception e) {
        throw new OrderServiceException("Product service is offline!", "PRODUCT_SERVICE_OFFLINE", 500);
    }

}
