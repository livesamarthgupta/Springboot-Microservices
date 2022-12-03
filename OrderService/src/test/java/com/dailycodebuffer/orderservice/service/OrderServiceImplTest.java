package com.dailycodebuffer.orderservice.service;

import com.dailycodebuffer.orderservice.entity.Order;
import com.dailycodebuffer.orderservice.exception.OrderServiceException;
import com.dailycodebuffer.orderservice.external.client.PaymentService;
import com.dailycodebuffer.orderservice.external.client.ProductService;
import com.dailycodebuffer.orderservice.external.request.PaymentRequest;
import com.dailycodebuffer.orderservice.external.response.PaymentResponse;
import com.dailycodebuffer.orderservice.external.response.ProductResponse;
import com.dailycodebuffer.orderservice.model.OrderRequest;
import com.dailycodebuffer.orderservice.model.OrderResponse;
import com.dailycodebuffer.orderservice.model.PaymentMode;
import com.dailycodebuffer.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @Test
    @DisplayName("Get Order - Success Scenario")
    void testGetOrderSuccess() {
        // Mocking
        Order order = getMockOrder();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class)).thenReturn(getMockProductResponse());
        when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class)).thenReturn(getMockPaymentResponse());

        // Actual
        OrderResponse orderResponse = orderService.getOrderByOrderId(1);

        // Verification
        verify(orderRepository, times(1)).findById(anyLong());
        verify(restTemplate, times(1)).getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class);
        verify(restTemplate, times(1)).getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class);

        // Assertion
        assertNotNull(orderResponse);
        assertEquals(order.getId(), orderResponse.getOrderId());
    }

    @Test
    @DisplayName("Get Order - Failure Scenario")
    void testGetOrderFailure() {
        // Mocking
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        // Actual/Assertion
        OrderServiceException exception = assertThrows(OrderServiceException.class, () -> orderService.getOrderByOrderId(1));
        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        // Verification
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Place Order - Success Scenario")
    void testPlaceOrderSuccess() {
        // Mocking
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();
        when(productService.reduceQuantity(anyLong(), anyLong())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentService.doPayment(any(PaymentRequest.class))).thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        // Actual
        long orderId = orderService.placeOrder(orderRequest);

        // Verification
        verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        // Assertion
        assertEquals(order.getId(), orderId);
    }

    @Test
    @DisplayName("Place Order - Payment Failure")
    void testPlaceOrderPaymentFailure() {
        // Mocking
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();
        when(productService.reduceQuantity(anyLong(), anyLong())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new OrderServiceException("Payment service is offline!", "PAYMENT_SERVICE_OFFLINE", 500));

        // Actual
        long orderId = orderService.placeOrder(orderRequest);

        // Verification
        verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        // Assertion
        assertEquals(order.getId(), orderId);
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .quantity(10)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(100)
                .build();
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .orderId(1)
                .status("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(1)
                .productName("iPhone")
                .price(100)
                .quantity(200)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(0)
                .amount(100)
                .quantity(200)
                .productId(1)
                .build();
    }

}