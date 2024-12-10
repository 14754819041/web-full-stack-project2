package com.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.constant.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Integer paymentType;
    private String paymentNo;
    private String transactionId;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 获取支付状态描述
     */
    public String getStatusDesc() {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getValue() == this.status) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }

    /**
     * 设置支付状态
    */
    public void setPaymentStatus(PaymentStatus status) {
        this.status = status.getValue();
    }
}
