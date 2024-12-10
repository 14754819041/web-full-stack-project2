package com.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer status;
    private BigDecimal totalAmount;
    private Date payTime;
    private String paymentNo;
    private Date deliveryTime;
    private Date finishTime;
    private Date createTime;
    private Date updateTime;
} 