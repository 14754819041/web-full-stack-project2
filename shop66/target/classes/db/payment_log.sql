-- 创建支付日志表
CREATE TABLE `payment_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `payment_no` varchar(64) NOT NULL COMMENT '支付流水号',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `payment_type` tinyint NOT NULL COMMENT '支付方式：1-支付宝，2-微信支付，3-银行卡',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型：CREATE-创建支付，NOTIFY-支付通知，SUCCESS-支付成功，FAIL-支付失败',
  `content` text COMMENT '日志内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付日志表'; 