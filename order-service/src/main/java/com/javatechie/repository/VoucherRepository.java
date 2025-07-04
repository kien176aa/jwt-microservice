package com.javatechie.repository;

import com.javatechie.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    @Query("select v from Voucher v where v.userId = :userId and v.used = false " +
            "and v.expiredAt > CURRENT_TIMESTAMP ")
    List<Voucher> findByUserId(Long userId);

    @Modifying
    @Query("update Voucher v set v.used = true where v.id = :id")
    void updateStatus(Long id);
}
