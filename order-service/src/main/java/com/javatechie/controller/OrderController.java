package com.javatechie.controller;

import com.javatechie.client.IdentityClient;
import com.javatechie.client.ProductClient;
import com.javatechie.entity.Order;
import com.javatechie.entity.Voucher;
import com.javatechie.repository.OrderRepository;
import com.javatechie.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final VoucherService voucherService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private IdentityClient identityClient;

    @PostMapping("/checkout")
    public CommonResponse<String> checkout(@RequestHeader("Authorization") String token,@RequestBody CheckoutRequest request) {
        CommonResponse<?> authResponse = identityClient.getCurrentUser(token);
        log.info("checkout: {}", authResponse);
        if(authResponse.getStatusCode() != HttpStatus.OK.value() || authResponse.getData() == null) {
            log.error("invalid token???");
            return CommonResponse.unAuth();
        }
        Long userId = null;
        if(authResponse.getData() instanceof UserDto) {
            userId = (((UserDto) authResponse.getData()).getId());
        } else{
            log.info(authResponse.getData().getClass().getName());
            return CommonResponse.unAuth();
        }
        log.info("start checkout: {}", userId);
        CommonResponse<List<CartItemDto>> response = productClient.getProductByUserId(userId);
        log.info("get product by user: {}", response);
        if (response.getStatusCode() != HttpStatus.OK.value() || response.getData().isEmpty()) {
            return CommonResponse.notOk("Cart is empty");
        }
        List<CartItemDto> selectedProducts = new ArrayList<>();
        double totalPrice = 0;
        for (CartItemDto item : response.getData()) {
            CartItemDto a = getSelectedProduct(item, request.getSelectedProducts());
            if (a != null) {
                System.out.println("Price: " + a.getPrice() + ", Quantity: " + a.getQuantity());
                totalPrice += a.getPrice() * a.getQuantity();
                selectedProducts.add(a);
            } else {
                System.out.println("Selected product not found for item: " + item);
            }
        }
        System.out.println("Total Price: " + totalPrice);
        Voucher voucher = voucherService.finbById(request.getVoucherId(), totalPrice);

        OrderDto order = new OrderDto(null, userId, LocalDateTime.now(), totalPrice,
                "PENDING", selectedProducts, setVoucher(voucher));
        log.info("Order info: {}", order);
        kafkaTemplate.send("order-topic", order);

        return CommonResponse.ok("Order placed successfully, processing...");
    }

    private VoucherDto setVoucher(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        VoucherDto voucherDto = new VoucherDto();
        voucherDto.setId(voucher.getId());
        voucherDto.setCode(voucher.getCode());
        voucherDto.setPercent(voucher.isPercent());
        voucherDto.setDiscountAmount(voucher.getDiscountAmount());
        return voucherDto;
    }

    @PostMapping("/my-order")
    public CommonResponse<SearchResponse<List<OrderResponse>>> myOrder(@RequestHeader("Authorization") String token,
                                                                       @RequestBody SearchRequest<String, Order> request) {
        CommonResponse<?> authResponse = identityClient.getCurrentUser(token);
        log.info("myOrder: {}", authResponse);
        if(authResponse.getStatusCode() != HttpStatus.OK.value() || authResponse.getData() == null) {
            log.error("myOrder invalid token???");
            return CommonResponse.unAuth();
        }
        Long userId = null;
        if(authResponse.getData() instanceof UserDto) {
            userId = (((UserDto) authResponse.getData()).getId());
        } else{
            log.info(authResponse.getData().getClass().getName());
            return CommonResponse.unAuth();
        }
        log.info("start myOrder: {}", userId);
        Page<Order> orders = orderRepository.findByUserIdWithPage(userId, request.getPageable(Order.class));
        SearchResponse<List<OrderResponse>> response = new SearchResponse<>();
        response.setData(orders.getContent().stream().map(item -> new OrderResponse(
                item.getId(),
                item.getUserId(),
                item.getOrderDate(),
                item.getTotalPrice(),
                item.getStatus(),
                item.getCartItemsJson(),
                item.getVoucherMess()
        )).toList());
        response.setPageSize(request.getPageSize());
        response.setPageIndex(request.getPageIndex());
        response.setTotalRecords(orders.getTotalElements());
        return CommonResponse.ok(response);
    }


    private CartItemDto getSelectedProduct(CartItemDto item, List<CartItemDto> request) {
        for (CartItemDto cart : request) {
            if(cart.getProductId().equals(item.getProductId())){
                CartItemDto a = new CartItemDto();
                a.setProductId(cart.getProductId());
                a.setQuantity(cart.getQuantity());
                a.setPrice(item.getPrice());
                a.setUserId(item.getUserId());
                return a;
            }
        }
        return null;
    }
}

