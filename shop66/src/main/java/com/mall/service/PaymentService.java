package com.mall.service;

import com.mall.dto.PaymentRequest;
import com.mall.dto.PaymentStatusDTO;
import com.mall.entity.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    String pay(PaymentRequest request);
    void handleCallback(String paymentNo, String transactionId, BigDecimal amount);
    PaymentStatusDTO getStatus(String paymentNo);
    List<Payment> getTimeoutPayments();
} 