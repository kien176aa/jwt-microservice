package com.javatechie.repository;

import com.javatechie.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("select c from CartItem c where c.userId = :userId")
    List<CartItem> searchByUserId(Long userId);
}
