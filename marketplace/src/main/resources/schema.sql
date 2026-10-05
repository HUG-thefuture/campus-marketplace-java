-- =====================================================================
-- 校园二手交易平台 建表脚本（schema.sql）
-- 设计要点：给高频等值过滤字段建索引（category / status / seller_id），保证分页查询性能；
-- 注意 LIKE '%关键字%' 因前缀通配符走不了 B+ 树索引，关键词检索属全表扫描（数据量小时可接受）
-- =====================================================================
USE marketplace;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(64)  NOT NULL COMMENT '用户名（唯一）',
    `password`      VARCHAR(128) NOT NULL COMMENT '密码（BCrypt 加密存储，绝不存明文）',
    `nickname`      VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER=普通用户 / ADMIN=管理员',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`         VARCHAR(128)  NOT NULL COMMENT '商品标题',
    `description`   TEXT          COMMENT '商品描述',
    `category`      VARCHAR(32)   NOT NULL COMMENT '分类（如 书籍/电子/生活）',
    `price`         DECIMAL(10,2) NOT NULL COMMENT '价格',
    `images`        TEXT          COMMENT '图片地址，JSON 数组字符串',
    `status`        VARCHAR(20)   NOT NULL DEFAULT 'ON_SALE' COMMENT '状态：ON_SALE=在售 / OFF_SHELF=已下架 / SOLD=已售出',
    `seller_id`     BIGINT        NOT NULL COMMENT '卖家用户ID',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_category` (`category`),       -- 分类检索索引
    KEY `idx_product_seller` (`seller_id`),        -- “我的商品”检索（越权判断也依赖它）
    KEY `idx_product_status` (`status`),           -- 按状态过滤
    KEY `idx_product_title` (`title`)              -- 服务标题精确/前缀匹配；LIKE '%kw%' 双侧通配符用不上该索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT   NOT NULL COMMENT '收藏者用户ID',
    `product_id`    BIGINT   NOT NULL COMMENT '被收藏商品ID',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_product` (`user_id`, `product_id`),  -- 防止重复收藏同一商品
    KEY `idx_favorite_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`      VARCHAR(64)   NOT NULL COMMENT '订单号（业务唯一）',
    `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `buyer_id`      BIGINT        NOT NULL COMMENT '买家用户ID',
    `seller_id`     BIGINT        NOT NULL COMMENT '卖家用户ID（冗余，便于卖家查询与越权判断）',
    `amount`        DECIMAL(10,2) NOT NULL COMMENT '成交金额（下单时快照）',
    `status`        VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PAID/COMPLETED/CANCELED',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orders_order_no` (`order_no`),
    KEY `idx_orders_buyer` (`buyer_id`),
    KEY `idx_orders_seller` (`seller_id`),
    KEY `idx_orders_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id`    BIGINT       NOT NULL COMMENT '被举报商品ID',
    `reporter_id`   BIGINT       NOT NULL COMMENT '举报人用户ID',
    `reason`        VARCHAR(255) NOT NULL COMMENT '举报原因',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING/RESOLVED/REJECTED',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_product` (`product_id`),
    KEY `idx_report_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';