package com.javatechie.repository;

import com.javatechie.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("select o from Order o where o.userId = :userId order by o.id desc ")
    List<Order> findByUserId(Long userId);
    @Query("select o from Order o where o.userId = :userId")
    Page<Order> findByUserIdWithPage(Long userId, Pageable pageable);

    @Query("""
    SELECT o FROM Order o
    WHERE o.userId = :userId
      AND (:status IS NULL OR o.status = :status)
      AND (:fromDate IS NULL OR o.orderDate >= :fromDate)
      AND (:toDate IS NULL OR o.orderDate <= :toDate)
      AND (:minPrice IS NULL OR o.totalPrice >= :minPrice)
      AND (:maxPrice IS NULL OR o.totalPrice <= :maxPrice)
      AND (:voucherMess IS NULL OR o.voucherMess LIKE %:voucherMess%)
    ORDER BY o.id DESC
""")
    List<Order> searchOrders(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("voucherMess") String voucherMess
    );

}
