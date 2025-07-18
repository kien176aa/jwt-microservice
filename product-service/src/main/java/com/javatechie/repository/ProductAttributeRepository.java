package com.javatechie.repository;

import com.javatechie.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    List<ProductAttribute> findByProductId(Long productId);
    void deleteByProductId(Long productId);

    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.productId IN :productIds")
    List<ProductAttribute> findByProductIdIn(@Param("productIds") List<Long> productIds);
}