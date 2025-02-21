package com.javatechie.repository;

import com.javatechie.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByName(String name);
    @Query("select p from Product p where :status is null or p.status = :status ")
    List<Product> search(Boolean status);
}
