package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.PaymentRequest;
import com.mall.dto.PaymentStatusDTO;
import com.mall.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid PaymentRequest request) {
        try {
            log.info("创建支付请求：{}", request);
            String payUrl = paymentService.pay(request);
            log.info("创建支付成功，支付链接：{}", payUrl);
            return Result.success(payUrl);
        } catch (Exception e) {
            log.error("创建支付失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/status/{paymentNo}")
    public Result<PaymentStatusDTO> getStatus(@PathVariable String paymentNo) {
        try {
            log.info("查询支付状态：paymentNo={}", paymentNo);
            PaymentStatusDTO status = paymentService.getStatus(paymentNo);
            log.info("查询支付状态成功：{}", status);
            return Result.success(status);
        } catch (Exception e) {
            log.error("查询支付状态失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/callback")
    public Result<Void> handleCallback(@RequestParam String paymentNo, @RequestParam String transactionId, @RequestParam BigDecimal amount) {
        try {
            log.info("支付回调：paymentNo={}, transactionId={}, amount={}", paymentNo, transactionId, amount);
            paymentService.handleCallback(paymentNo, transactionId, amount);
            log.info("处理支付回调成功");
            return Result.success();
        } catch (Exception e) {
            log.error("处理支付回调失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
} 