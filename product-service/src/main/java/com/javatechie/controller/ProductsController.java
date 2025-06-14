package com.javatechie.controller;

import com.javatechie.dto.ProductDto;
import com.javatechie.dto.SearchProductRequest;
import com.javatechie.clients.IdentityClient;
import com.javatechie.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ErrorMessage;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.example.exception.UnAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductsController {
    @Autowired
    private ProductService productService;
    @Autowired
    private IdentityClient identityClient;

    @PostMapping("/add")
    public ProductDto createProduct(@RequestBody ProductDto product) {
        return productService.createProduct(product);
    }

    @PostMapping ("/search")
    public List<ProductDto> getAllProducts(SearchProductRequest request) {
        return productService.getAllProducts(request);
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
    CommonResponse<String> decreaseStock(@RequestBody List<CartItemDto> cartItems) throws Exception {
        log.info("start decreaseStock: {}", cartItems);
        return CommonResponse.ok(productService.decreaseStock(cartItems));
    }
}

