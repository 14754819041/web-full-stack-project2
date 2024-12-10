package com.mall.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentStatusDTO {
    private String paymentNo;
    private Integer status;
    private String statusDesc;
    private BigDecimal amount;
    private Date payTime;
} 