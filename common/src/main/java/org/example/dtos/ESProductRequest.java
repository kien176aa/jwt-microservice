package org.example.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class ESProductRequest {
    private String keyword;
    private Double minPrice;
    private Double maxPrice;
    private Map<String, String> attributes;
    private int page = 0;
    private int size = 8;
    private String orderBy;
    private String sortBy;
}
