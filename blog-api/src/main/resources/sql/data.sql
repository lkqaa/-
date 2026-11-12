-- ============================================================
-- Initial data
-- NOTE: admin password is "123456" (BCrypt encoded)
-- ============================================================

USE `blog`;

-- Admin: username=admin, password=123456 (BCrypt)
INSERT INTO `admin` (`username`, `password`, `nickname`, `email`)
VALUES ('admin', '$2a$10$k4Jj6r6.syFjOekPxfhb8.MZhhT0.ex/TJFdtcdHtqZbbrnaUWBWC', 'Blog Admin', 'admin@example.com');

-- Categories
INSERT INTO `category` (`name`, `slug`, `description`, `sort_order`) VALUES
('技术', 'tech', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2),
('随笔', 'essay', '思考与感悟', 3);

-- Tags
INSERT INTO `tag` (`name`, `color`) VALUES
('Java', '#f89820'),
('Spring Boot', '#6db33f'),
('Vue', '#42b883'),
('前端', '#35495e'),
('后端', '#5d4037');

-- Sample article (category_id=1, tag Java + Spring Boot)
INSERT INTO `article` (`title`, `summary`, `content`, `category_id`, `status`, `published_at`, `slug`) VALUES
('你好，世界！', '这是博客系统的第一篇文章。', '# Hello Blog\n\n这是 **个人博客系统** 的第一篇文章。\n\n```java\npublic static void main(String[] args) {\n    System.out.println("Hello, Blog!");\n}\n```\n', 1, 1, NOW(), 'hello-world');

INSERT INTO `article_tag` (`article_id`, `tag_id`) VALUES (1, 1), (1, 2);

-- System config
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('blog_title', '我的个人博客', '博客标题'),
('blog_subtitle', '记录学习与生活', '博客副标题'),
('blog_logo', '', '博客Logo URL'),
('about_me', '<h2>关于我</h2><p>一个热爱技术的开发者。</p>', '关于我页面内容');
