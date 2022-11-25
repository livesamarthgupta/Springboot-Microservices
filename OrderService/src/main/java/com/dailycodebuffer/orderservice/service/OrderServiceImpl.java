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
import com.dailycodebuffer.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Checking product quantity with id:{}", orderRequest.getProductId());
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

       log.info("Placing order request: {}", orderRequest);
       Order order = Order.builder()
               .orderDate(Instant.now())
               .orderStatus("CREATED")
               .quantity(orderRequest.getQuantity())
               .amount(orderRequest.getTotalAmount())
               .productId(orderRequest.getProductId())
               .build();

       orderRepository.save(order);

       log.info("Redirecting to payment service...");
       PaymentRequest paymentRequest = PaymentRequest.builder()
               .orderId(order.getId())
               .amount(orderRequest.getTotalAmount())
               .paymentMode(orderRequest.getPaymentMode())
               .build();
       String orderStatus = null;
       try {
           paymentService.doPayment(paymentRequest);
           log.info("Payment successful: Changing the order status to PLACED");
           orderStatus = "PLACED";
       } catch (Exception e) {
           log.error("Payment failed: Changing the order status to PAYMENT_FAILED");
           orderStatus = "PAYMENT_FAILED";
       }

       order.setOrderStatus(orderStatus);
       orderRepository.save(order);

       log.info("Order placed successfully with orderId: {}", order.getId());
       return order.getId();
    }

    @Override
    public OrderResponse getOrderByOrderId(long orderId) {
        log.info("Fetching order details with id:{}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("No order found with given id!", "ORDER_NOT_FOUND", 404));

        log.info("Fetching product details for product id:{}", order.getProductId());
        ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class);

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productId(productResponse.getProductId())
                .productName(productResponse.getProductName())
                .build();

        log.info("Fetching payment details for order id:{}", order.getId());
        PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class);

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentDate(paymentResponse.getPaymentDate())
                .status(paymentResponse.getStatus())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();


        return OrderResponse.builder()
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .orderId(order.getId())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
    }
}
