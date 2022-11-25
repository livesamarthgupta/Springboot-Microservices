package com.dailycodebuffer.productservice.service;

import com.dailycodebuffer.productservice.entity.Product;
import com.dailycodebuffer.productservice.exception.ProductServiceException;
import com.dailycodebuffer.productservice.model.ProductRequest;
import com.dailycodebuffer.productservice.model.ProductResponse;
import com.dailycodebuffer.productservice.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.*;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Saving product with request: {}", productRequest);
        Product product = Product.builder()
                .productName(productRequest.getName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();
        productRepository.save(product);
        log.info("Product saved with id:{}", product.getProductId());

        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Fetching product with id:{}", productId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductServiceException("Product with given id not found!", "PRODUCT_NOT_FOUND", 404));
        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);
        log.info("Fetched product: {}", product);

        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reducing quantity:{} for product id:{}", quantity, productId);
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductServiceException("Product with given id not found!", "PRODUCT_NOT_FOUND", 404));

        if(product.getQuantity() < quantity) {
            throw new ProductServiceException("Product doesn't have sufficient quantity!", "INSUFFICIENT_QUANTITY", 400);
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Quantity reduced by:{} for product id:{}", quantity, productId);
    }
}
