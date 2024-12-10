package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
    @Select("SELECT * FROM payment WHERE payment_no = #{paymentNo}")
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);
} 