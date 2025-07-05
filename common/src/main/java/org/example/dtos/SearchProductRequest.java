package org.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchProductRequest {
    private String name;
    private String nameOp;
    private Double price;
    private String priceOp;
    private Integer quantity;
    private String quantityOp;
    private String sortBy;
    private String orderBy;
    private Boolean status;
}
