package com.javatechie.service;

import com.javatechie.client.ProductClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.example.dtos.DecreaseStockRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductClient productClient;

    @Retry(name = "decreaseStockRetry", fallbackMethod = "fallbackDecreaseStock")
    @CircuitBreaker(name = "decreaseStockCB", fallbackMethod = "fallbackDecreaseStock")
    public CommonResponse<?> decreaseStock(DecreaseStockRequest request) {
        log.info("ProductService: decrease stock request: {}", request);
        return productClient.decreaseStock(request);
    }

    public CommonResponse<?> fallbackDecreaseStock(DecreaseStockRequest request, Throwable ex) {
        log.error("Fallback triggered for decreaseStock due to: {}", ex.getMessage(), ex);
        return CommonResponse.notOk("Product is unavailable!!!");
    }

    @Retry(name = "getProductByUserRetry", fallbackMethod = "fallbackGetProductByUser")
    @CircuitBreaker(name = "getProductByUserCB", fallbackMethod = "fallbackGetProductByUser")
    public CommonResponse<List<CartItemDto>> getProductByUserId(Long userId) {
        log.info("ProductService: getProductByUserId request: {}", userId);
        return productClient.getProductByUserId(userId);
    }

    public CommonResponse<List<CartItemDto>> fallbackGetProductByUser(Long userId, Throwable ex) {
        log.error("Fallback triggered for getProductByUserId due to: {}", ex.getMessage(), ex);
        return CommonResponse.notOkWithMess(null,"Product is unavailable!!!");
    }

}
