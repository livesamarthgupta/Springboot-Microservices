package com.dailycodebuffer.orderservice.controller;

import com.dailycodebuffer.orderservice.model.OrderRequest;
import com.dailycodebuffer.orderservice.model.OrderResponse;
import com.dailycodebuffer.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('Customer')")
    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest) {
        long orderId = orderService.placeOrder(orderRequest);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByOrderId(@PathVariable long orderId) {
        OrderResponse orderResponse = orderService.getOrderByOrderId(orderId);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

}
