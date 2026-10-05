-- =====================================================================
-- 校园二手交易平台 示例数据（data.sql）
-- 说明：本文件只灌「结构类」示例数据（商品/收藏/订单/举报，外键依赖 user id=1,2）。
--       两个预置账号 user01 / admin（密码均为 123456）改由应用启动时的
--       DataInitializer 用 BCrypt 现场生成正确哈希——避免在 SQL 里硬编码
--       一个"看起来像但可能不匹配"的密码哈希。
-- =====================================================================
USE marketplace;

-- 商品（卖家均为 user01，id=1）；商品 1 已有示例 PENDING 订单，状态为 SOLD 保持一致
INSERT INTO `product` (`id`, `title`, `description`, `category`, `price`, `images`, `status`, `seller_id`) VALUES
(1, '高等数学教材（第7版）', '大一下学期用，几乎全新，无笔记', '书籍', 25.00, '["/img/book1.jpg"]', 'SOLD', 1),
(2, '九成新iPad 2020', '自用，无磕碰，带原装充电器', '电子', 1500.00, '["/img/ipad1.jpg","/img/ipad2.jpg"]', 'ON_SALE', 1),
(3, '宿舍台灯', '可调光，毕业出', '生活', 15.00, '["/img/lamp1.jpg"]', 'OFF_SHELF', 1)
ON DUPLICATE KEY UPDATE title = title;

-- 示例收藏：user01 收藏了商品 2（仅演示数据存在性）
INSERT INTO `favorite` (`id`, `user_id`, `product_id`) VALUES (1, 1, 2)
ON DUPLICATE KEY UPDATE id = id;

-- 示例订单：一笔 PENDING 订单（买家 admin=2，卖家 user01=1）
INSERT INTO `orders` (`id`, `order_no`, `product_id`, `buyer_id`, `seller_id`, `amount`, `status`) VALUES
(1, 'MP20250101000001', 1, 2, 1, 25.00, 'PENDING')
ON DUPLICATE KEY UPDATE id = id;

-- 示例举报
INSERT INTO `report` (`id`, `product_id`, `reporter_id`, `reason`, `status`) VALUES
(1, 3, 2, '商品图片与描述不符', 'PENDING')
ON DUPLICATE KEY UPDATE id = id;