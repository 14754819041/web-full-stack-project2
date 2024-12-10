-- 禁用安全模式
SET SQL_SAFE_UPDATES = 0;

-- 清理现有数据
DELETE FROM product;
DELETE FROM category;
ALTER TABLE product AUTO_INCREMENT = 1;
ALTER TABLE category AUTO_INCREMENT = 1;

-- 插入一级分类
INSERT INTO category (name, parent_id, level, sort, create_time, update_time) VALUES
('手机数码', null, 1, 1, NOW(), NOW()),
('家用电器', null, 1, 2, NOW(), NOW()),
('电脑办公', null, 1, 3, NOW(), NOW());

-- 插入二级分类
INSERT INTO category (name, parent_id, level, sort, create_time, update_time) VALUES
('手机', 1, 2, 1, NOW(), NOW()),
('平板电脑', 1, 2, 2, NOW(), NOW()),
('数码配件', 1, 2, 3, NOW(), NOW()),
('冰箱', 2, 2, 1, NOW(), NOW()),
('洗衣机', 2, 2, 2, NOW(), NOW()),
('空调', 2, 2, 3, NOW(), NOW()),
('笔记本', 3, 2, 1, NOW(), NOW()),
('台式机', 3, 2, 2, NOW(), NOW()),
('办公设备', 3, 2, 3, NOW(), NOW());

-- 插入商品数据（使用正确的分类ID）
INSERT INTO product (category_id, name, price, stock, description, main_image, images, sales, status, create_time, update_time) VALUES
(4, '华为P50', 4999.00, 100, '华为最新5G手机', '/images/huawei_p50.jpg', '/images/p50_1.jpg,/images/p50_2.jpg', 0, 1, NOW(), NOW()),
(4, 'OPPO Find X5', 3999.00, 200, 'OPPO新品旗舰', '/images/oppo_x5.jpg', '/images/x5_1.jpg,/images/x5_2.jpg', 0, 1, NOW(), NOW()),
(9, '格力空调', 2999.00, 50, '节能变频空调', '/images/geli_ac.jpg', '/images/ac_1.jpg,/images/ac_2.jpg', 0, 1, NOW(), NOW()),
(8, '美的洗衣机', 1999.00, 80, '智能洗衣机', '/images/meidi_wm.jpg', '/images/wm_1.jpg,/images/wm_2.jpg', 0, 1, NOW(), NOW());

-- 恢复安全模式
SET SQL_SAFE_UPDATES = 1; 