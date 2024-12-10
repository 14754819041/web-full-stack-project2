-- 关闭安全模式
SET SQL_SAFE_UPDATES = 0;

-- 清理商品和分类数据
DELETE FROM product;
DELETE FROM category;

-- 重置自增ID
ALTER TABLE product AUTO_INCREMENT = 1;
ALTER TABLE category AUTO_INCREMENT = 1;

-- 恢复安全模式
SET SQL_SAFE_UPDATES = 1; 