package com.mall.service.impl;

import com.mall.constant.OrderStatus;
import com.mall.constant.PaymentStatus;
import com.mall.constant.PaymentType;
import com.mall.dto.PaymentRequest;
import com.mall.dto.PaymentStatusDTO;
import com.mall.entity.Order;
import com.mall.entity.Payment;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.PaymentMapper;
import com.mall.service.OrderService;
import com.mall.service.PaymentService;
import com.mall.service.PaymentLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final int PAYMENT_TIMEOUT_MINUTES = 30;  // 添加超时时间常量

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayServiceImpl alipayService;

    @Autowired
    private PaymentLogService paymentLogService;

    @Override
    @Transactional
    public String pay(PaymentRequest request) {
        // 1. 检查订单状态
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.UNPAID.getValue()) {
            throw new RuntimeException("订单状态不正确，当前状态：" + OrderStatus.getDesc(order.getStatus()));
        }

        // 2. 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(order.getUserId());
        payment.setPaymentType(request.getPaymentType());
        payment.setPaymentNo(generatePaymentNo());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        // 记录支付创建日志
        paymentLogService.logPaymentCreate(payment.getPaymentNo(), "创建支付订单");

        // 3. 调用第三方支付接口
        try {
            String payUrl = callThirdPartyPay(payment);
            log.info("创建支付订单成功：paymentNo={}, payUrl={}", payment.getPaymentNo(), payUrl);
            return payUrl;
        } catch (Exception e) {
            // 记录支付失败日志
            paymentLogService.logPaymentFail(payment.getPaymentNo(), e.getMessage());
            log.error("调用支付接口失败", e);
            throw new RuntimeException("支付失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleCallback(String paymentNo, String transactionId, BigDecimal amount) {
        log.info("处理支付回调：paymentNo={}, transactionId={}, amount={}", 
            paymentNo, transactionId, amount);

        // 1. 查询支付记录
        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        // 2. 验证支付金额
        if (payment.getAmount().compareTo(amount) != 0) {
            log.error("支付金额不匹配：期望={}, 实际={}", payment.getAmount(), amount);
            throw new RuntimeException("支付金额不匹配");
        }

        // 3. 更新支付状态
        payment.setStatus(PaymentStatus.SUCCESS.getValue());
        payment.setTransactionId(transactionId);
        payment.setPayTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        // 4. 更新订单状态
        orderService.paySuccess(
            payment.getOrderId().toString(),  // 转换为String
            transactionId,
            amount
        );

        // 5. 记录支付日志
        paymentLogService.logPaymentSuccess(paymentNo);

        log.info("支付回调处理成功：paymentNo={}", paymentNo);
    }

    // 生成支付流水号
    private String generatePaymentNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 调用第三方支付接口
    private String callThirdPartyPay(Payment payment) {
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        switch (payment.getPaymentType()) {
            case PaymentType.ALIPAY:
                return alipayService.createPayment(payment, order);
            case PaymentType.WECHAT_PAY:
                // TODO: 实现微信支付
                return "weixin://wxpay/bizpayurl?pr=" + payment.getPaymentNo();
            case PaymentType.BANK_CARD:
                // TODO: 实现银行卡支付
                return "https://bank.example.com/pay?orderNo=" + payment.getPaymentNo();
            default:
                throw new RuntimeException("不支持的支付方式");
        }
    }

    @Override
    public PaymentStatusDTO getStatus(String paymentNo) {
        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        PaymentStatusDTO dto = new PaymentStatusDTO();
        dto.setPaymentNo(payment.getPaymentNo());
        dto.setStatus(payment.getStatus());
        dto.setStatusDesc(payment.getStatusDesc());
        dto.setAmount(payment.getAmount());
        dto.setPayTime(Date.from(payment.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()));
        
        return dto;
    }

    @Override
    public List<Payment> getTimeoutPayments() {
        QueryWrapper<Payment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", PaymentStatus.PENDING.getValue())
                   .apply("TIMESTAMPDIFF(MINUTE, create_time, NOW()) > {0}", PAYMENT_TIMEOUT_MINUTES);
        return paymentMapper.selectList(queryWrapper);
    }
}




