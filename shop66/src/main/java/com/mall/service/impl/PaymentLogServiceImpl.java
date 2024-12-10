package com.mall.service.impl;

import com.mall.entity.PaymentLog;
import com.mall.mapper.PaymentLogMapper;
import com.mall.service.PaymentLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentLogServiceImpl implements PaymentLogService {

    @Autowired
    private PaymentLogMapper paymentLogMapper;

    @Override
    public void logPaymentCreate(String paymentNo, String message) {
        saveLog(paymentNo, "CREATE", message);
    }

    @Override
    public void logPaymentSuccess(String paymentNo) {
        saveLog(paymentNo, "SUCCESS", "支付成功");
    }

    @Override
    public void logPaymentFail(String paymentNo, String reason) {
        saveLog(paymentNo, "FAIL", "支付失败：" + reason);
    }

    private void saveLog(String paymentNo, String type, String message) {
        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setPaymentNo(paymentNo);
        paymentLog.setType(type);
        paymentLog.setMessage(message);
        paymentLog.setCreateTime(LocalDateTime.now());
        paymentLogMapper.insert(paymentLog);
        log.info("保存支付日志：paymentNo={}, type={}, message={}", paymentNo, type, message);
    }
}
