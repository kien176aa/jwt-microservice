package com.javatechie.client;

import org.example.dtos.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductClient {

    @GetMapping("/products/get-by-user/{userId}")
    CommonResponse<List<CartItemDto>> getProductByUserId(@PathVariable Long userId);
    @PostMapping("/products/decrease-stock")
    CommonResponse<?> decreaseStock(@RequestBody DecreaseStockRequest request);
    @PostMapping("/products/search")
    List<ProductDto> searchProducts(@RequestBody SearchProductRequest request);
}

