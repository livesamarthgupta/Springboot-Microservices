package com.dailycodebuffer.productservice.model;

import lombok.Data;

@Data
public class ProductRequest {
    private long productId;
    private String name;
    private long price;
    private long quantity;
}
