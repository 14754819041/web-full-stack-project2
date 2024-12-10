package com.mall.constant;

public class PaymentType {
    public static final int ALIPAY = 1;    // 支付宝
    public static final int WECHAT_PAY = 2; // 微信支付
    public static final int BANK_CARD = 3;  // 银行卡

    public static String getDesc(Integer type) {
        switch (type) {
            case ALIPAY: return "支付宝";
            case WECHAT_PAY: return "微信支付";
            case BANK_CARD: return "银行卡";
            default: return "未知支付方式";
        }
    }
} 