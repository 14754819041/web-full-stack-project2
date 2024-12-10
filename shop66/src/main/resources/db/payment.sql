-- 创建支付记录表
CREATE TABLE IF NOT EXISTS `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `payment_type` tinyint NOT NULL COMMENT '支付方式：1-支付宝，2-微信支付，3-银行卡',
  `payment_no` varchar(64) NOT NULL COMMENT '支付流水号',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_no` varchar(64) NOT NULL COMMENT '订单号',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态：0待支付，1已支付，2待发货，3已发货，4已完成，5已取消',
    `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `payment_no` varchar(64) DEFAULT NULL COMMENT '支付流水号',
    `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
    `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建订单项表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_id` bigint(20) NOT NULL COMMENT '订单ID',
    `product_id` bigint(20) NOT NULL COMMENT '商品ID',
    `product_name` varchar(100) NOT NULL COMMENT '商品名称',
    `product_image` varchar(200) DEFAULT NULL COMMENT '商品图片',
    `price` decimal(10,2) NOT NULL COMMENT '商品单价',
    `quantity` int(11) NOT NULL COMMENT '购买数量',
    `total_price` decimal(10,2) NOT NULL COMMENT '总价',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 创建支付日志表
CREATE TABLE IF NOT EXISTS `payment_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` varchar(64) NOT NULL COMMENT '支付单号',
    `type` varchar(20) NOT NULL COMMENT '日志类型：CREATE-创建，SUCCESS-成功，FAIL-失败',
    `message` varchar(255) DEFAULT NULL COMMENT '日志信息',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付日志表';