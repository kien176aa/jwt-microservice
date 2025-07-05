package com.javatechie.dto;

import lombok.Data;

@Data
public class OrderFilter {
    private Long userId;
    private String status;
    private Double minTotalPrice;
    private Double maxTotalPrice;
    private String fromDate;
    private String toDate;
    private String voucherMess;
    private String fieldsToSelect;
}
