-- ============================================================
-- Personal Blog Schema
-- MySQL 8.4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `blog`;

-- ---------- admin ----------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `username`        VARCHAR(32)  NOT NULL COMMENT 'Login account',
    `password`        VARCHAR(128) NOT NULL COMMENT 'BCrypt hash',
    `nickname`        VARCHAR(64)  DEFAULT NULL COMMENT 'Display name',
    `avatar`          VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    `email`           VARCHAR(128) DEFAULT NULL,
    `last_login_time` DATETIME     DEFAULT NULL,
    `last_login_ip`   VARCHAR(64)  DEFAULT NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Administrator';

-- ---------- category ----------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(64)  NOT NULL,
    `slug`        VARCHAR(64)  NOT NULL COMMENT 'URL alias',
    `description` VARCHAR(255) DEFAULT NULL,
    `sort_order`  INT          DEFAULT 0,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Category';

-- ---------- tag ----------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(32) NOT NULL,
    `color`      VARCHAR(16) DEFAULT NULL,
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tag';

-- ---------- article ----------
DROP TABLE IF EXISTS `article_tag`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `title`        VARCHAR(255)  NOT NULL,
    `summary`      VARCHAR(500)  DEFAULT NULL,
    `content`      LONGTEXT      NOT NULL COMMENT 'Markdown source',
    `html_content` LONGTEXT      DEFAULT NULL COMMENT 'Rendered HTML',
    `cover`        VARCHAR(255)  DEFAULT NULL,
    `category_id`  BIGINT        DEFAULT NULL,
    `slug`         VARCHAR(128)  DEFAULT NULL,
    `status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '0=draft, 1=published',
    `views`        INT           DEFAULT 0,
    `comment_count` INT          DEFAULT 0,
    `is_top`       TINYINT       DEFAULT 0 COMMENT '0=no, 1=yes',
    `published_at` DATETIME      DEFAULT NULL,
    `deleted`      TINYINT       DEFAULT 0 COMMENT '0=normal, 1=deleted',
    `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status_published` (`status`, `published_at` DESC),
    KEY `idx_views` (`views` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Article';

-- ---------- article_tag ----------
CREATE TABLE `article_tag` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `tag_id`     BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Article-Tag relation';

-- ---------- comment ----------
CREATE TABLE `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT       NOT NULL,
    `nickname`   VARCHAR(64)  NOT NULL,
    `email`      VARCHAR(128) DEFAULT NULL,
    `website`    VARCHAR(255) DEFAULT NULL,
    `content`    TEXT         NOT NULL,
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0=pending, 1=approved, 2=spam',
    `ip`         VARCHAR(64)  DEFAULT NULL,
    `user_agent` VARCHAR(500) DEFAULT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Comment';

-- ---------- view_log ----------
DROP TABLE IF EXISTS `view_log`;
CREATE TABLE `view_log` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT       NOT NULL COMMENT 'Article ID',
    `ip`         VARCHAR(64)  DEFAULT NULL COMMENT 'Visitor IP',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'Visitor user agent',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Article view log';

-- ---------- sys_config ----------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`   VARCHAR(64)  NOT NULL,
    `config_value` TEXT,
    `description`  VARCHAR(255) DEFAULT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System config';
