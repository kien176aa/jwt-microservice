package com.javatechie.service;

import com.javatechie.dto.OrderFilter;
import com.javatechie.entity.Order;
import com.javatechie.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Map<String, Object>> searchOrdersFlexible(OrderFilter filter) {
        // Convert string -> field list
        Set<String> selectedFields = Arrays.stream(
                Optional.ofNullable(filter.getFieldsToSelect())
                        .orElse("id,userId,orderDate,status,totalPrice")
                        .split(",")
        ).map(String::trim).collect(Collectors.toSet());

        // Query full Order entities
        List<Order> orders = orderRepository.searchOrders(
                filter.getUserId(),
                filter.getStatus(),
                parseDate(filter.getFromDate()),
                parseDate(filter.getToDate()),
                filter.getMinTotalPrice(),
                filter.getMaxTotalPrice(),
                filter.getVoucherMess()
        );

        // Chuyển mỗi Order -> Map có các trường được chọn
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> map = new HashMap<>();
            if (selectedFields.contains("id")) map.put("id", order.getId());
            if (selectedFields.contains("userId")) map.put("userId", order.getUserId());
            if (selectedFields.contains("orderDate")) map.put("orderDate", order.getOrderDate());
            if (selectedFields.contains("status")) map.put("status", order.getStatus());
            if (selectedFields.contains("totalPrice")) map.put("totalPrice", order.getTotalPrice());
            if (selectedFields.contains("voucherMess")) map.put("voucherMess", order.getVoucherMess());
            if (selectedFields.contains("cartItemsJson")) map.put("cartItemsJson", order.getCartItemsJson());
            result.add(map);
        }

        return result;
    }

    private LocalDateTime parseDate(String s) {
        return s != null ? LocalDateTime.parse(s) : null;
    }
}
