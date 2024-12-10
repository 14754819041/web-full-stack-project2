-- 插入测试分类数据
INSERT INTO category (name, parent_id, level, sort, create_time, update_time) VALUES
('手机数码', null, 1, 1, NOW(), NOW()),
('家用电器', null, 1, 2, NOW(), NOW()),
('电脑办公', null, 1, 3, NOW(), NOW());

-- 插入测试商品数据
INSERT INTO product (category_id, name, price, stock, description, main_image, images, sales, status, create_time, update_time) VALUES
(1, '小米手机12', 2999.00, 100, '小米最新旗舰手机', '/images/mi12.jpg', '/images/mi12_1.jpg,/images/mi12_2.jpg', 0, 1, NOW(), NOW()),
(1, 'iPhone 13', 5999.00, 50, 'Apple最新手机', '/images/iphone13.jpg', '/images/iphone13_1.jpg,/images/iphone13_2.jpg', 0, 1, NOW(), NOW()),
(2, '海尔冰箱', 3999.00, 30, '节能环保冰箱', '/images/haier.jpg', '/images/haier_1.jpg,/images/haier_2.jpg', 0, 1, NOW(), NOW()); 

-- 更新商品图片
UPDATE product SET image = '/images/huawei_p50.jpg' WHERE id = 1;
UPDATE product SET image = '/images/xiaomi_12.jpg' WHERE id = 2;
UPDATE product SET image = '/images/iphone_13.jpg' WHERE id = 3; 