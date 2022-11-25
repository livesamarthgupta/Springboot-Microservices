package com.dailycodebuffer.productservice.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductServiceException extends RuntimeException {

    private String errorCode;
    private int status;

    public ProductServiceException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
