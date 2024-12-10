package com.mall.service;

public interface PaymentLogService {
    void logPaymentCreate(String paymentNo, String message);
    void logPaymentSuccess(String paymentNo);
    void logPaymentFail(String paymentNo, String reason);
} 