-- 微服务 DEMO 数据库初始化脚本
-- 初始数据将由 UaaApplication 启动时的 InitConfig 自动插入，此处无需手动 INSERT
-- 1. 认证中心数据库 (uaa_db)
CREATE DATABASE IF NOT EXISTS `uaa_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `uaa_db`;
-- 创建用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密密码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `roles` varchar(255) DEFAULT NULL COMMENT '角色列表(多个角色用逗号分隔)',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限表';

-- 2. 产品中心数据库 (product_db)
CREATE DATABASE IF NOT EXISTS `product_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `product_db`;
-- 创建产品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品信息表';
