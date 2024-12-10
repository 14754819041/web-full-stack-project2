package com.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.Result;
import com.mall.dto.OrderCreateRequest;
import com.mall.dto.OrderDTO;
import com.mall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<OrderDTO> create(@RequestBody @Valid OrderCreateRequest request) {
        try {
            log.info("创建订单请求：{}", request);
            OrderDTO order = orderService.create(request.getUserId(), request.getCartIds());
            log.info("创建订单成功：{}", order);
            return Result.success(order);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Page<OrderDTO>> list(@RequestParam Long userId,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            return Result.success(orderService.list(userId, status, pageNum, pageSize));
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDTO> detail(@PathVariable Long id) {
        try {
            return Result.success(orderService.detail(id));
        } catch (Exception e) {
            log.error("查询订单详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        try {
            orderService.pay(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        try {
            orderService.cancel(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        try {
            orderService.confirm(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id) {
        try {
            orderService.deliver(id);
            return Result.success(null);
        } catch (Exception e) {
            log.error("订单发货失败", e);
            return Result.error(e.getMessage());
        }
    }
} 