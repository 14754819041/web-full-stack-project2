package com.mall.service.impl;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.mall.entity.Order;
import com.mall.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlipayServiceImpl {

    public String createPayment(Payment payment, Order order) {
        try {
            AlipayTradePagePayResponse response = Factory.Payment.Page()
                .pay(order.getOrderNo(), 
                     payment.getAmount().toString(), 
                     "商城订单-" + order.getOrderNo(),
                     "");
            
            log.info("创建支付宝支付订单成功：orderNo={}, paymentNo={}, response={}", 
                order.getOrderNo(), payment.getPaymentNo(), response.getBody());
            
            return response.getBody();
        } catch (Exception e) {
            log.error("创建支付宝支付订单失败：", e);
            throw new RuntimeException("创建支付订单失败：" + e.getMessage());
        }
    }
} 
