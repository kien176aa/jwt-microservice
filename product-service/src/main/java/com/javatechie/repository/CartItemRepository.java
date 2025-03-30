package com.javatechie.repository;

import com.javatechie.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("select c from CartItem c where c.userId = :userId")
    List<CartItem> searchByUserId(Long userId);

    @Modifying
    @Query("delete from CartItem c where c.userId = :userId")
    void deleteByUserId(Long userId);
    @Modifying
    @Query("delete from CartItem c where c.userId = :userId and c.productId in (:productIds)")
    void deleteByIds(List<Long> productIds, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
