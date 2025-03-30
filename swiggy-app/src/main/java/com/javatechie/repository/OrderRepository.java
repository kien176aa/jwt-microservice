package com.javatechie.repository;

import com.javatechie.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o where o.userId = :userId order by o.id desc ")
    List<Order> findByUserId(Long userId);
}
