package com.dailycodebuffer.paymentservice.service;

import com.dailycodebuffer.paymentservice.entity.Transaction;
import com.dailycodebuffer.paymentservice.model.PaymentMode;
import com.dailycodebuffer.paymentservice.model.PaymentRequest;
import com.dailycodebuffer.paymentservice.model.PaymentResponse;
import com.dailycodebuffer.paymentservice.repository.TransactionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording payment request: {}", paymentRequest);
        Transaction transaction = Transaction.builder()
                .paymentMode(paymentRequest.getPaymentMode().name())
                .amount(paymentRequest.getAmount())
                .paymentDate(Instant.now())
                .paymentStatus("SUCCESS")
                .referenceNumber(paymentRequest.getReferenceNumber())
                .orderId(paymentRequest.getOrderId())
                .build();

        transactionRepository.save(transaction);
        log.info("Payment successful with id:{}", transaction.getId());
        return transaction.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(long orderId) {
        log.info("Fetching payment details with order id:{}", orderId);
        Transaction transaction = transactionRepository.findByOrderId(orderId);

        return PaymentResponse.builder()
                .paymentMode(PaymentMode.valueOf(transaction.getPaymentMode()))
                .paymentDate(transaction.getPaymentDate())
                .orderId(transaction.getOrderId())
                .paymentId(transaction.getId())
                .amount(transaction.getAmount())
                .status(transaction.getPaymentStatus())
                .build();
    }
}
