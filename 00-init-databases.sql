-- 00-init-databases.sql
-- MySQL 首次初始化时最先执行（文件名 00 开头）：创建两个项目各自的数据库。
-- 后续 01/02/03 号脚本中的建表语句依赖这里的库。
CREATE DATABASE IF NOT EXISTS marketplace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS shortlink    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
