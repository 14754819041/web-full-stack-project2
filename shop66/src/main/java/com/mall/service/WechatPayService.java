package com.mall.service;

import java.math.BigDecimal;

public interface WechatPayService {
    /**
     * 创建微信支付订单
     *
     * @param orderId     商户订单号
     * @param amount      支付金额
     * @param description 商品描述
     * @return 支付链接
     */
    String createOrder(String orderId, BigDecimal amount, String description);
    
    /**
     * 验证微信支付回调通知
     *
     * @param notifyData    通知数据
     * @param signature     签名
     * @param nonce        随机字符串
     * @param timestamp    时间戳
     * @param serialNumber 证书序列号
     * @return 验证结果
     */
    boolean verifyNotify(String notifyData, String signature, String nonce, String timestamp, String serialNumber);
} 