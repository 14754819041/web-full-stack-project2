-- 检查并更新payment表的status字段注释
ALTER TABLE `payment` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '0' 
COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已关闭';

-- 检查并更新orders表的status字段注释
ALTER TABLE `orders` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '0' 
COMMENT '订单状态：0-待支付，1-已支付，2-待发货，3-已发货，4-已完成，5-已取消';

-- 检查并更新payment表的payment_type字段注释
ALTER TABLE `payment` MODIFY COLUMN `payment_type` tinyint NOT NULL 
COMMENT '支付方式：1-支付宝，2-微信支付';

-- 检查payment_log表是否存在type字段
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
AND table_name = 'payment_log' 
AND column_name = 'type';

-- 如果type字段不存在，则添加
SET @sql = CASE WHEN @col_exists = 0 
    THEN 'ALTER TABLE payment_log ADD COLUMN `type` varchar(20) NOT NULL DEFAULT "CREATE" COMMENT "日志类型" AFTER payment_no'
    ELSE 'SELECT "字段 type 已存在"'
END;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加索引
SELECT COUNT(*) INTO @idx_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'payment_log' 
AND index_name = 'idx_type';

SET @sql = CASE WHEN @idx_exists = 0 
    THEN 'ALTER TABLE payment_log ADD INDEX idx_type (type)'
    ELSE 'SELECT "索引 idx_type 已存在"'
END;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加payment表的用户ID索引
SELECT COUNT(*) INTO @idx_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'payment' 
AND index_name = 'idx_user_id';

SET @sql = CASE WHEN @idx_exists = 0 
    THEN 'ALTER TABLE payment ADD INDEX idx_user_id (user_id)'
    ELSE 'SELECT "索引 idx_user_id 已存在"'
END;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加交易号字段
ALTER TABLE payment 
ADD COLUMN transaction_id varchar(64) DEFAULT NULL COMMENT '第三方交易号' AFTER payment_no;

-- 添加支付时间字段
ALTER TABLE payment 
ADD COLUMN pay_time datetime DEFAULT NULL COMMENT '支付时间' AFTER status; 