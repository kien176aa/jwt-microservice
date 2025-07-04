package com.javatechie.repository;

import com.javatechie.entity.UpdateQuantityTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdateQuantityTransactionRepository extends JpaRepository<UpdateQuantityTransaction, Long> {
    boolean existsUpdateQuantityTransactionByTransactionIdAndProductId(String transactionId, Long productId);
}
