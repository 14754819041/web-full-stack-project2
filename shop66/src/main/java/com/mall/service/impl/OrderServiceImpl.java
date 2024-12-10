package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.constant.OrderStatus;
import com.mall.dto.OrderDTO;
import com.mall.dto.OrderItemDTO;
import com.mall.entity.Cart;
import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import com.mall.entity.Product;
import com.mall.mapper.CartMapper;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    // 订单超时时间（分钟）
    private static final int ORDER_TIMEOUT_MINUTES = 1;

    @Override
    @Transactional
    public OrderDTO create(Long userId, List<Long> cartIds) {
        log.info("创建订单开始：userId={}, cartIds={}", userId, cartIds);
        
        // 1. 获取购物车商品
        List<Cart> cartList = getCartList(userId, cartIds);
        if (cartList.isEmpty()) {
            log.error("购物车为空：userId={}, cartIds={}", userId, cartIds);
            throw new RuntimeException("购物车为空");
        }
        log.info("获取购物车商品成功：size={}", cartList.size());

        // 2. 检查商品库存
        checkStock(cartList);
        log.info("商品库存检查通过");

        // 3. 计算订单总金额
        BigDecimal totalAmount = calculateTotalAmount(cartList);
        log.info("计算订单总金额：totalAmount={}", totalAmount);

        // 4. 创建订单
        Order order = createOrder(userId, totalAmount);
        log.info("创建订单成功：orderId={}", order.getId());

        // 5. 创建订单项
        List<OrderItem> orderItems = createOrderItems(order.getId(), cartList);
        log.info("创建订单项成功：size={}", orderItems.size());

        // 6. 更新商品库存和销量
        updateProductStock(cartList);
        log.info("更新商品库存和销量成功");

        // 7. 清空购物车
        clearCart(userId, cartIds);
        log.info("清空购物车成功");

        // 8. 返回订单信息
        return convertToDTO(order, orderItems);
    }

    private List<Cart> getCartList(Long userId, List<Long> cartIds) {
        log.info("查询购物车商品：userId={}, cartIds={}", userId, cartIds);
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .in("id", cartIds)
               .eq("selected", true);
        List<Cart> cartList = cartMapper.selectList(wrapper);
        log.info("查询到购物车商品：{}", cartList);
        return cartList;
    }

    // 添加DTO转换方法
    private OrderDTO convertToDTO(Order order, List<OrderItem> orderItems) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(item, itemDTO);
            return itemDTO;
        }).collect(Collectors.toList());
        
        dto.setItems(itemDTOs);
        return dto;
    }

    @Override
    public Page<OrderDTO> list(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");
        
        // 查询订单
        Page<Order> orderPage = orderMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO
        Page<OrderDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(orderPage, dtoPage, "records");
        
        List<OrderDTO> dtoList = orderPage.getRecords().stream().map(order -> {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            return convertToDTO(order, items);
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public OrderDTO detail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return convertToDTO(order, items);
    }

    @Override
    @Transactional
    public void pay(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != OrderStatus.UNPAID.getValue()) {
            throw new RuntimeException("订单状态不正确，当前状态：" + OrderStatus.getDesc(order.getStatus()));
        }
        
        order.setStatus(OrderStatus.PAID.getValue());
        order.setPayTime(new Date());
        order.setUpdateTime(new Date());
        orderMapper.updateById(order);
        
        log.info("订单支付成功：orderId={}, orderNo={}", order.getId(), order.getOrderNo());
    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有待付款状态(0)和待发货状态(1)的订单才能取消
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确，当前状态：" + OrderStatus.getDesc(order.getStatus()));
        }
        
        order.setStatus(OrderStatus.CANCELLED.getValue());
        order.setUpdateTime(new Date());
        orderMapper.updateById(order);
        
        // 恢复商品库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
        
        log.info("订单取消成功：orderId={}, orderNo={}", order.getId(), order.getOrderNo());
    }

    @Override
    @Transactional
    public void confirm(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有待收货状态(2)的订单才能确认收货
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，当前状态：" + OrderStatus.getDesc(order.getStatus()));
        }
        
        order.setStatus(OrderStatus.COMPLETED.getValue());
        order.setFinishTime(new Date());
        order.setUpdateTime(new Date());
        orderMapper.updateById(order);
        
        // 更新商品销量
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setSales(product.getSales() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
        
        log.info("订单确认收货成功：orderId={}, orderNo={}", order.getId(), order.getOrderNo());
    }

    // 添加订单发货方法
    @Override
    @Transactional
    public void deliver(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有待发货状态(1)的订单才能发货
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确，当前状态：" + OrderStatus.getDesc(order.getStatus()));
        }
        
        order.setStatus(OrderStatus.DELIVERED.getValue());
        order.setDeliveryTime(new Date());
        order.setUpdateTime(new Date());
        orderMapper.updateById(order);
        
        log.info("订单发货成功：orderId={}, orderNo={}", order.getId(), order.getOrderNo());
    }

    // 修改超时订单查询方法
    public List<Order> getTimeoutOrders() {
        // 使用数据库时间进行比较
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0)  // 待付款状态
                   .apply("TIMESTAMPDIFF(MINUTE, create_time, NOW()) > {0}", ORDER_TIMEOUT_MINUTES);
                   
        List<Order> orders = orderMapper.selectList(queryWrapper);
        log.info("查询到{}个超时订单", orders.size());
        return orders;
    }

    @Override
    public void paySuccess(String orderId, String transactionId, BigDecimal amount) {
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }
        
        // 验证订单状态
        if (order.getStatus() != OrderStatus.UNPAID.getValue()) {
            log.warn("订单状态不正确，orderId: {}, status: {}", orderId, order.getStatus());
            return;
        }
        
        // 验证支付金额
        if (order.getTotalAmount().compareTo(amount) != 0) {
            log.error("支付金额不匹配，orderId: {}, orderAmount: {}, paidAmount: {}", 
                    orderId, order.getTotalAmount(), amount);
            throw new RuntimeException("支付金额不匹配");
        }
        
        // 更新订单状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatus.PAID.getValue());
        updateOrder.setPayTime(new Date());
        updateOrder.setPaymentNo(transactionId);
        updateOrder.setUpdateTime(new Date());
        
        orderMapper.updateById(updateOrder);
        
        log.info("订单支付成功：orderId={}, transactionId={}", orderId, transactionId);
    }

    // 检查商品库存
    private void checkStock(List<Cart> cartList) {
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在：" + cart.getProductId());
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足：" + product.getName());
            }
        }
    }

    // 计算订单总金额
    private BigDecimal calculateTotalAmount(List<Cart> cartList) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        }
        return totalAmount;
    }

    // 创建订单
    private Order createOrder(Long userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.UNPAID.getValue());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        orderMapper.insert(order);
        return order;
    }

    // 创建订单项
    private List<OrderItem> createOrderItems(Long orderId, List<Cart> cartList) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            orderItemMapper.insert(item);
            orderItems.add(item);
        }
        return orderItems;
    }

    // 更新商品库存和销量
    private void updateProductStock(List<Cart> cartList) {
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            product.setStock(product.getStock() - cart.getQuantity());
            product.setSales(product.getSales() + cart.getQuantity());
            productMapper.updateById(product);
        }
    }

    // 清空购物车
    private void clearCart(Long userId, List<Long> cartIds) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .in("id", cartIds);
        cartMapper.delete(wrapper);
    }

    // 生成订单号
    private String generateOrderNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }
} 