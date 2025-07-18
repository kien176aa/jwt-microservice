package com.javatechie.dto;

import com.javatechie.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSyncPage {
    private List<Product> products;
    private boolean hasMore;
}
