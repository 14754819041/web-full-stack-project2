package com.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dto.OrderDTO;
import com.mall.entity.Order;
import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderDTO create(Long userId, List<Long> cartIds);
    Page<OrderDTO> list(Long userId, Integer status, Integer pageNum, Integer pageSize);
    OrderDTO detail(Long orderId);
    void pay(Long orderId);
    void cancel(Long orderId);
    void confirm(Long orderId);
    void deliver(Long orderId);
    List<Order> getTimeoutOrders();
    /**
     * 处理支付成功
     *
     * @param orderId        商户订单号
     * @param transactionId  微信支付订单号
     * @param amount        支付金额
     */
    void paySuccess(String orderId, String transactionId, BigDecimal amount);
} 