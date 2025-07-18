package com.javatechie.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.javatechie.batch.SyncProductToES;
import com.javatechie.dto.ProductDto;
import com.javatechie.dto.ProductSyncPage;
import com.javatechie.entity.Product;
import com.javatechie.entity.ProductESDocument;
import com.javatechie.service.ProductESService;
import org.example.dtos.*;
import com.javatechie.clients.IdentityClient;
import com.javatechie.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.UnAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductsController {
    @Autowired
    private ProductService productService;
    @Autowired
    private IdentityClient identityClient;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private ProductESService productESService;

    @GetMapping("/sync-products")
    public CommonResponse<String> syncProducts() {
        int threadLimit = 10;
        int limit = 10;
        int offset = 0;

        ExecutorService executor = Executors.newFixedThreadPool(threadLimit);
        List<Future<?>> futures = new ArrayList<>();

        try {
            productESService.deleteAllProductsFromES();
            while (true) {
                ProductSyncPage productSyncPage = productService.getProductToSync(limit, offset);
                if (productSyncPage.getProducts().isEmpty()) break;

                SyncProductToES task = context.getBean(SyncProductToES.class);
                task.setProducts(productSyncPage.getProducts());

                try {
                    Future<?> future = executor.submit(task);
                    futures.add(future);
                } catch (Exception e) {
                    log.error("Failed to submit task for offset {}", offset, e);
                }

                if (!productSyncPage.isHasMore()) break;
                offset++;
            }

            // Đợi tất cả task hoàn tất, nhưng timeout tổng là 10 phút
            executor.shutdown();
            boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
            if (!finished) {
                log.warn("Timeout reached. Forcing shutdown now...");
                executor.shutdownNow(); // Hủy những task đang chạy
            }

        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for executor shutdown", e);
            Thread.currentThread().interrupt();
        }

        return CommonResponse.ok("ok");
    }


    @PostMapping("/add")
    public ProductDto createProduct(@RequestBody ProductDto product) {
        return productService.createProduct(product);
    }

    @PostMapping ("/search")
    public List<ProductDto> getAllProducts(@RequestBody SearchProductRequest request) {
        return productService.getAllProducts(request);
    }

    @PostMapping ("/e-search")
    public org.example.dtos.SearchResponse<List<ProductDto>> elasticSearch(@RequestBody ESProductRequest request) {
        return productService.searchProducts(request);
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable Long id) {
        return new ProductDto(productService.getProductById(id));
    }

    @PutMapping("/update/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto updatedProduct) {
        return productService.updateProduct(id, updatedProduct);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Product deleted successfully!";
    }

    @PostMapping("/add-to-cart")
    public CommonResponse<?> addToCart(@RequestHeader("Authorization") String token, @RequestBody CartItemDto item) {
        log.info("AddToCart1: {}", item);
        CommonResponse<?> response = identityClient.getCurrentUser(token);
        log.info("AddToCart2: {}", response);
        if(response.getStatusCode() != HttpStatus.OK.value() || response.getData() == null) {
            log.error("invalid token???");
            return CommonResponse.unAuth();
        }
        if(response.getData() instanceof UserDto) {
            item.setUserId(((UserDto) response.getData()).getId());
        } else{
          log.info(response.getData().getClass().getName());
        }
        return CommonResponse.ok(productService.addToCart(item));
    }

    @DeleteMapping("/remove-from-cart")
    public CommonResponse<?> removeFromCart(@RequestHeader("Authorization") String token, @RequestParam Long productId) {
        log.info("RemoveFromCart1: {}", productId);
        CommonResponse<?> response = identityClient.getCurrentUser(token);
        log.info("RemoveFromCart2: {}", response);
        if(response.getStatusCode() != HttpStatus.OK.value() || response.getData() == null) {
            log.error("invalid token1???");
            return CommonResponse.unAuth();
        }
        Long userId = null;
        if(response.getData() instanceof UserDto) {
            userId = (((UserDto) response.getData()).getId());
        }else {
            log.info(response.getData().getClass().getName());
            log.error("invalid token2???");
            return CommonResponse.unAuth();
        }
        return productService.removeFromCart(userId, productId);
    }

    @GetMapping("/get-by-user/{userId}")
    public CommonResponse<?> getProductByUserId(@PathVariable Long userId){
        log.info("getProductByUserId: {}", userId);
        return CommonResponse.ok(productService.getProductByUserId(userId));
    }

    @GetMapping("/my-cart")
    public CommonResponse<?> getProductByMyCart(@RequestHeader("Authorization") String token){
        CommonResponse<?> response = identityClient.getCurrentUser(token);
        log.info("AddToCart2: {}", response);
        if(response.getStatusCode() != HttpStatus.OK.value() || response.getData() == null) {
            log.error("invalid token???");
            return CommonResponse.unAuth();
        }
        if(response.getData() instanceof UserDto) {
            return CommonResponse.ok(productService.getProductByUserId(((UserDto) response.getData()).getId()));
        } else{
            log.info(response.getData().getClass().getName());
            throw new UnAuthException();
        }
    }

    @PostMapping("/decrease-stock")
    CommonResponse<String> decreaseStock(@RequestBody DecreaseStockRequest request) throws Exception {
        log.info("start decreaseStock: {}", request);
        return CommonResponse.ok(productService.decreaseStock(request));
    }
}

