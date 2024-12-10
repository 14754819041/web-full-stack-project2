package com.mall.constant;

import lombok.Getter;

@Getter
public enum OrderStatus {
    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    PENDING_DELIVERY(2, "待发货"),
    DELIVERED(3, "已发货"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int value;
    private final String desc;

    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static String getDesc(int value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue() == value) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }
}