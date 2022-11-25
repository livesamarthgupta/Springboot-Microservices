package com.dailycodebuffer.orderservice.external.decoder;


import com.dailycodebuffer.orderservice.exception.OrderServiceException;
import com.dailycodebuffer.orderservice.external.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;


@Log4j2
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        log.info("::{}", response.request().url());
        log.info("::{}", response.request().headers());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ErrorResponse errorResponse = objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
            return new OrderServiceException(errorResponse.getErrorMessage(), errorResponse.getErrorCode(), response.status());
        } catch (IOException e) {
            throw new OrderServiceException("Internal server error!", "INTERNAL_SERVER_ERROR", 500);
        }
    }
}
