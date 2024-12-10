package com.mall.task;

import com.mall.entity.Payment;
import com.mall.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PaymentTask {

    @Autowired
    private PaymentService paymentService;

    @Scheduled(fixedDelay = 60000)
    public void handleTimeoutPayments() {
        log.info("开始处理超时支付...");
        try {
            List<Payment> timeoutPayments = paymentService.getTimeoutPayments();
            for (Payment payment : timeoutPayments) {
                try {
                    paymentService.handleCallback(
                        payment.getPaymentNo(),
                        null,
                        payment.getAmount()
                    );
                    log.info("处理超时支付：paymentNo={}", payment.getPaymentNo());
                } catch (Exception e) {
                    log.error("处理超时支付失败: paymentNo=" + payment.getPaymentNo(), e);
                }
            }
        } catch (Exception e) {
            log.error("处理超时支付任务失败", e);
        }
    }
} 
