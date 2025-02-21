package com.javatechie.dto;


import com.javatechie.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private String image;
    private Integer quantity;
    private String description;
    private Boolean status;

    public ProductDto(Product p){
        id = p.getId();
        name = p.getName();
        price = p.getPrice();
        quantity = p.getQuantity();
        image = p.getImage();
        description = p.getDescription();
        status = p.getStatus();
    }
}
