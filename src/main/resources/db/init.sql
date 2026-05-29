-- 创建数据库
CREATE DATABASE IF NOT EXISTS health_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE health_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `age`         INT          NULL COMMENT '年龄',
    `username`    VARCHAR(10)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `height`      DOUBLE       NULL COMMENT '身高(cm)',
    `weight`      DOUBLE       NULL COMMENT '体重(kg)',
    `goal`        VARCHAR(255) NULL COMMENT '健康目标',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';
