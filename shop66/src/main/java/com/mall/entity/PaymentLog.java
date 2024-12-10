package com.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_log")
public class PaymentLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String paymentNo;
    private String type;
    private String message;
    private LocalDateTime createTime;
} 