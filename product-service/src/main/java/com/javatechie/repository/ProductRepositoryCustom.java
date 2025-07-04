package com.javatechie.repository;

import com.javatechie.entity.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> searchWithOperators(String name, String nameOp,
                                      Double price, String priceOp,
                                      Integer quantity, String quantityOp,
                                      Boolean status,
                                      String sortBy, String sortDir);
}
