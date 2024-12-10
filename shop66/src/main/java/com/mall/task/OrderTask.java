package com.mall.task;

import com.mall.entity.Order;
import com.mall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;

    // 每分钟执行一次
    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelTimeoutOrders() {
        try {
            log.info("开始检查超时订单... 当前时间: {}", LocalDateTime.now());
            List<Order> timeoutOrders = orderService.getTimeoutOrders();
            
            if (timeoutOrders.isEmpty()) {
                log.info("没有发现超时订单");
                return;
            }
            
            log.info("发现{}个超时订单，开始处理...", timeoutOrders.size());
            for (Order order : timeoutOrders) {
                try {
                    log.info("处理超时订单: orderId={}, orderNo={}, createTime={}", 
                        order.getId(), order.getOrderNo(), order.getCreateTime());
                    orderService.cancel(order.getId());
                    log.info("超时订单已取消: orderId={}, orderNo={}", 
                        order.getId(), order.getOrderNo());
                } catch (Exception e) {
                    log.error("取消超时订单失败: orderId=" + order.getId(), e);
                }
            }
            
            log.info("超时订单处理完成，共处理{}个订单", timeoutOrders.size());
        } catch (Exception e) {
            log.error("超时订单处理任务异常", e);
        }
    }
} 