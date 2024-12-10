package com.mall.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class PaymentRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "支付方式不能为空")
    private Integer paymentType;  // 1-支付宝，2-微信支付
} 