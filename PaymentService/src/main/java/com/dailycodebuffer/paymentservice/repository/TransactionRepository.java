package com.dailycodebuffer.paymentservice.repository;

import com.dailycodebuffer.paymentservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByOrderId(long orderId);
}
