-- =====================================================================
-- 短链接服务 建表脚本（schema.sql）
-- 设计要点：
--   * short_code 用唯一索引，保证短码唯一
--   * expire_at 建索引，方便批量清理过期数据 + 查询时走索引
--   * 访问次数用原子更新（access_count = access_count + 1）
-- =====================================================================
USE shortlink;

CREATE TABLE IF NOT EXISTS `short_url` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `short_code`   VARCHAR(16)  NOT NULL COMMENT '短码（Base62，唯一）',
    `original_url` VARCHAR(512) NOT NULL COMMENT '原始长链接',
    `expire_at`    DATETIME     NOT NULL COMMENT '失效时间',
    `access_count` BIGINT       NOT NULL DEFAULT 0 COMMENT '访问次数',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_short_url_code` (`short_code`),
    KEY `idx_short_url_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短链接表';