package com.javatechie.client;

import org.example.constants.ConstantValue;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = ConstantValue.URL_PRODUCT_SERVICE)
public interface ProductClient {

    @GetMapping("/products/get-by-user/{userId}")
    CommonResponse<List<CartItemDto>> getProductByUserId(@PathVariable Long userId);
    @PostMapping("/products/decrease-stock")
    CommonResponse<?> decreaseStock(@RequestBody List<CartItemDto> cartItems);
}

