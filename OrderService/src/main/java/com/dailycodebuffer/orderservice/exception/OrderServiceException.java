package com.dailycodebuffer.orderservice.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderServiceException extends RuntimeException {
    private String errorCode;
    private int status;

    public OrderServiceException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
