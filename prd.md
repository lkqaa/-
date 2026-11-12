# 个人博客系统 PRD（产品需求文档）

> 版本：v1.0  
> 日期：2026-11-10  
> 技术栈：Vue3 + Element Plus / Spring Boot 3 + MyBatis-Plus / MySQL 8.0

---

## 一、需求分析

### 1.1 项目概述

构建一个面向个人博主的全栈博客系统，包含两个独立子工程：

| 工程 | 定位 | 技术栈 |
|------|------|--------|
| **blog-frontend**（用户端） | 面向访客的博客浏览站点 | Vue3 + Vite + Element Plus + Vue Router + Pinia |
| **blog-admin**（管理后台） | 面向博主的内容管理系统 | Vue3 + Vite + Element Plus + Vue Router + Pinia |
| **blog-api**（后端服务） | 提供 RESTful API | Spring Boot 3.x + Java 17 + MyBatis-Plus + MySQL 8.0 + JWT + Redis（可选） |

---

### 1.2 用户端功能清单

| 模块 | 功能点 | 说明 |
|------|--------|------|
| **首页** | 文章列表（分页） | 按发布时间倒序，支持每页条数切换（10/20/50） |
| | 分类导航 | 侧边栏显示所有分类，点击筛选 |
| | 标签云 | 侧边栏显示标签及文章数，点击筛选 |
| | 热门文章 | 侧边栏显示访问量 Top 5 |
| | 搜索 | 关键词匹配文章标题与摘要 |
| **文章详情** | 正文渲染 | Markdown → HTML，代码高亮（highlight.js） |
| | 元信息 | 发布时间、分类、标签、阅读量、评论数 |
| | 阅读量统计 | 每次进入详情页 +1 |
| | 上一篇/下一篇 | 同分类或全部文章的前后导航 |
| | 评论区 | 匿名/昵称评论，支持 Markdown，按时间倒序 |
| **关于我** | 个人介绍 | 静态页面，富文本内容 |
| | 联系方式 | GitHub / 邮箱 / 微信二维码等 |

---

### 1.3 管理后台功能清单

| 模块 | 功能点 | 说明 |
|------|--------|------|
| **登录** | 管理员登录 | 账号密码 + JWT，Token 有效期 7 天 |
| | 登录态保持 | Token 存 localStorage，请求拦截器自动携带 |
| | 路由守卫 | 未登录跳转登录页 |
| **仪表盘** | 数据统计卡片 | 文章总数、分类数、标签数、评论数、今日访问量 |
| | 图表 | 近 7 天/30 天文章发布趋势、访问量趋势（ECharts） |
| | 热门文章榜 | 访问量 Top 10 |
| **文章管理** | 文章列表 | 标题、分类、标签、状态（已发布/草稿）、创建时间、阅读量 |
| | 新建文章 | Markdown 编辑器（v-md-editor / md-editor-v3），实时预览 |
| | 编辑文章 | 同新建，回填已有内容 |
| | 发布/撤回 | 切换 status 字段 |
| | 删除文章 | 软删除 |
| | 批量操作 | 批量发布/草稿/删除 |
| **分类管理** | 分类列表 | 名称、别名（slug）、描述、文章数 |
| | 新增/编辑/删除 | 简单 CRUD，删除前校验是否有关联文章 |
| **标签管理** | 标签列表 | 名称、颜色、文章数 |
| | 新增/编辑/删除 | 简单 CRUD |
| **评论管理** | 评论列表 | 昵称、邮箱、文章、内容、时间、状态 |
| | 审核/删除 | 支持标记为垃圾评论或删除 |
| **系统设置** | 博客基本信息 | 标题、副标题、Logo、Favicon |
| | 关于我内容 | 富文本编辑 |

---

### 1.4 非功能性需求

| 类别 | 要求 |
|------|------|
| **安全性** | 管理后台单用户策略（固定账号）；密码 BCrypt 哈希存储；JWT 鉴权 + Refresh Token；接口权限拦截；SQL 注入/XSS 防护 |
| **性能** | 首页列表、文章详情、分类标签列表走 Redis 缓存（可选但推荐）；接口响应时间 < 300ms |
| **SEO** | 文章详情页支持动态设置 `<title>`、`<meta description>`；URL 友好（`/article/{id}-{slug}`） |
| **响应式** | 用户端支持 PC / Pad / Mobile 三端适配 |
| **可维护性** | 后端分层清晰（Controller → Service → Mapper）；统一响应结构；全局异常处理；API 文档（springdoc + Swagger UI） |

---

## 二、数据库设计

### 2.1 ER 关系概览

```
admin ──(1)──< article >──(N)── category
                 │
                 └──(M:N)── tag ──< article_tag

article ──(1)──< comment
article ──(1)──< view_log
```

---

### 2.2 表结构详细设计

#### 2.2.1 admin（管理员表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(32) | UNIQUE, NOT NULL | 登录账号 |
| password | VARCHAR(128) | NOT NULL | BCrypt 哈希 |
| nickname | VARCHAR(64) | | 昵称 |
| avatar | VARCHAR(255) | | 头像 URL |
| email | VARCHAR(128) | | 邮箱 |
| last_login_time | DATETIME | | 最后登录时间 |
| last_login_ip | VARCHAR(64) | | 最后登录 IP |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

> 单用户场景，初始化时插入一条记录即可。

---

#### 2.2.2 category（分类表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(64) | NOT NULL | 分类名称 |
| slug | VARCHAR(64) | UNIQUE, NOT NULL | URL 别名，英文/拼音 |
| description | VARCHAR(255) | | 分类描述 |
| sort_order | INT | DEFAULT 0 | 排序值，越小越靠前 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

---

#### 2.2.3 tag（标签表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(32) | UNIQUE, NOT NULL | 标签名称 |
| color | VARCHAR(16) | | 标签颜色（HEX） |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

---

#### 2.2.4 article（文章表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| title | VARCHAR(255) | NOT NULL | 文章标题 |
| summary | VARCHAR(500) | | 摘要（可选，自动截取正文前 N 字） |
| content | LONGTEXT | NOT NULL | Markdown 原文 |
| html_content | LONGTEXT | | 渲染后的 HTML（可选，冗余字段加速展示） |
| cover | VARCHAR(255) | | 封面图 URL |
| category_id | BIGINT | FK → category.id | 所属分类 |
| slug | VARCHAR(128) | UNIQUE | URL 友好别名 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 0=草稿, 1=已发布 |
| views | INT | DEFAULT 0 | 阅读量 |
| comment_count | INT | DEFAULT 0 | 评论数（冗余） |
| is_top | TINYINT | DEFAULT 0 | 是否置顶 0=否 1=是 |
| published_at | DATETIME | | 发布时间 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除 0=否 1=是 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：`idx_category_id`(category_id)、`idx_status_published`(status, published_at DESC)、`idx_views`(views DESC)

---

#### 2.2.5 article_tag（文章-标签关联表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| article_id | BIGINT | FK → article.id, NOT NULL | 文章 ID |
| tag_id | BIGINT | FK → tag.id, NOT NULL | 标签 ID |

**唯一约束**：`(article_id, tag_id)`

---

#### 2.2.6 comment（评论表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| article_id | BIGINT | FK → article.id, NOT NULL | 所属文章 |
| nickname | VARCHAR(64) | NOT NULL | 评论者昵称 |
| email | VARCHAR(128) | | 邮箱 |
| website | VARCHAR(255) | | 个人网站 |
| content | TEXT | NOT NULL | 评论内容（支持 Markdown 子集） |
| status | TINYINT | NOT NULL, DEFAULT 1 | 0=待审核, 1=已通过, 2=垃圾 |
| ip | VARCHAR(64) | | 评论者 IP |
| user_agent | VARCHAR(500) | | UA |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**：`idx_article_id`(article_id)、`idx_status`(status)

---

#### 2.2.7 sys_config（系统配置表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| config_key | VARCHAR(64) | UNIQUE, NOT NULL | 配置键 |
| config_value | TEXT | | 配置值 |
| description | VARCHAR(255) | | 说明 |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

> 预置 key：`blog_title`、`blog_subtitle`、`blog_logo`、`about_me`（关于我富文本）

---

#### 2.2.8 view_log（文章访问日志表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| article_id | BIGINT | FK → article.id, NOT NULL | 被访问的文章 |
| ip | VARCHAR(64) | | 访问者 IP |
| user_agent | VARCHAR(500) | | 访问者 UA |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 访问时间 |

**索引**：`idx_article_id`(article_id)、`idx_created_at`(created_at)

> 每次调用 `POST /articles/{id}/view` 写入一条记录；仪表盘「今日访问量」按 `created_at >= 当天 0 点` 统计。

---

## 三、接口规划

### 3.1 通用约定

#### 3.1.1 基础信息

| 项目 | 值 |
|------|----|
| Base URL（开发） | `http://localhost:8080/api` |
| 认证方式 | Bearer Token（JWT） |
| 认证头 | `Authorization: Bearer {token}` |
| Content-Type | `application/json` |

#### 3.1.2 统一响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 / Token 无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

#### 3.1.3 分页响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  }
}
```

---

### 3.2 认证模块（无需 Token）

| Method | Path | 说明 | 请求体 | 响应 data |
|--------|------|------|--------|-----------|
| POST | `/auth/login` | 管理员登录 | `{ username, password }` | `{ token, expiresIn, adminInfo }` |
| POST | `/auth/logout` | 退出登录 | - | `null` |
| GET | `/auth/info` | 获取当前登录用户信息 | Header: Authorization | `{ id, username, nickname, avatar }` |

---

### 3.3 用户端接口（公开访问）

#### 3.3.1 文章

| Method | Path | 说明 | 查询参数 | 响应 data |
|--------|------|------|----------|-----------|
| GET | `/articles` | 文章列表（分页） | `page, size, categoryId, tagId, keyword, orderBy=views/date` | Page\<ArticleVO\> |
| GET | `/articles/{id}` | 文章详情 | - | ArticleDetailVO（含 tags, category, 上一篇/下一篇） |
| POST | `/articles/{id}/view` | 增加阅读量 | - | `{ views }` |

#### 3.3.2 分类

| Method | Path | 说明 | 响应 data |
|--------|------|------|-----------|
| GET | `/categories` | 全部分类（含文章数） | List\<CategoryVO\> |
| GET | `/categories/{id}` | 分类详情 | CategoryVO |

#### 3.3.3 标签

| Method | Path | 说明 | 响应 data |
|--------|------|------|-----------|
| GET | `/tags` | 全部标签（含文章数） | List\<TagVO\> |

#### 3.3.4 评论

| Method | Path | 说明 | 请求体 | 响应 data |
|--------|------|------|--------|-----------|
| GET | `/comments` | 某文章评论列表 | Query: `articleId, page, size` | Page\<CommentVO\> |
| POST | `/comments` | 提交评论 | `{ articleId, nickname, email, website, content }` | CommentVO |

#### 3.3.5 系统配置

| Method | Path | 说明 | 响应 data |
|--------|------|------|-----------|
| GET | `/config` | 获取公开配置（博客标题、关于我等） | `{ blogTitle, blogSubtitle, blogLogo, aboutMe }` |

---

### 3.4 管理端接口（需 Token）

#### 3.4.1 文章管理

| Method | Path | 说明 | 请求体 / 参数 |
|--------|------|------|---------------|
| GET | `/admin/articles` | 后台文章列表（含筛选、状态） | Query: `page, size, title, categoryId, status` |
| GET | `/admin/articles/{id}` | 文章详情（含标签） | - |
| POST | `/admin/articles` | 新建文章 | `{ title, summary, content, categoryId, tagIds[], cover, slug, status }` |
| PUT | `/admin/articles/{id}` | 更新文章 | 同新建 |
| DELETE | `/admin/articles/{id}` | 删除文章（软删除） | - |
| PUT | `/admin/articles/{id}/status` | 更新状态 | `{ status }` |
| PUT | `/admin/articles/batch-delete` | 批量删除 | `{ ids: [1,2,3] }` |

#### 3.4.2 分类管理

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/categories` | 分类列表 |
| POST | `/admin/categories` | 新增分类 |
| PUT | `/admin/categories/{id}` | 更新分类 |
| DELETE | `/admin/categories/{id}` | 删除分类（校验关联文章） |

#### 3.4.3 标签管理

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/tags` | 标签列表 |
| POST | `/admin/tags` | 新增标签 |
| PUT | `/admin/tags/{id}` | 更新标签 |
| DELETE | `/admin/tags/{id}` | 删除标签 |

#### 3.4.4 评论管理

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/comments` | 评论列表（含筛选） |
| PUT | `/admin/comments/{id}/status` | 审核/标记垃圾 |
| DELETE | `/admin/comments/{id}` | 删除评论 |

#### 3.4.5 仪表盘统计

| Method | Path | 说明 | 响应 data |
|--------|------|------|-----------|
| GET | `/admin/stats/overview` | 概览统计 | `{ articleCount, categoryCount, tagCount, commentCount, todayViews, totalViews }` |
| GET | `/admin/stats/trend` | 趋势数据 | Query: `range=7d/30d` → `[{ date, articleCount, views }]` |
| GET | `/admin/stats/hot-articles` | 热门文章 Top 10 | List\<{ id, title, views }\> |

#### 3.4.6 系统设置

| Method | Path | 说明 |
|--------|------|------|
| GET | `/admin/config` | 获取全部配置 |
| PUT | `/admin/config` | 批量更新配置 |

---

### 3.5 VO 关键字段定义

#### ArticleVO（列表用）
```json
{
  "id": 1,
  "title": "文章标题",
  "summary": "摘要...",
  "cover": "https://...",
  "categoryId": 1,
  "categoryName": "技术",
  "tags": [{ "id": 1, "name": "Vue" }],
  "views": 1234,
  "commentCount": 5,
  "publishedAt": "2026-11-01T10:00:00",
  "isTop": 1
}
```

#### ArticleDetailVO（详情用）
```json
{
  "...ArticleVO 全部字段": "...",
  "content": "## Markdown 原文",
  "htmlContent": "<h2>渲染后的 HTML</h2>",
  "prev": { "id": 10, "title": "上一篇标题" },
  "next": { "id": 12, "title": "下一篇标题" }
}
```

---

## 四、项目工程结构建议

```
个人博客/
├── blog-frontend/          # 用户端（Vue3）
├── blog-admin/             # 管理后台（Vue3）
├── blog-api/               # 后端服务（Spring Boot 3）
│   ├── src/main/java/com/example/blog/
│   │   ├── controller/     # REST 控制器
│   │   │   ├── api/        # 公开接口
│   │   │   └── admin/      # 管理接口
│   │   ├── service/        # 业务层
│   │   ├── mapper/         # MyBatis-Plus Mapper
│   │   ├── entity/         # 数据库实体
│   │   ├── dto/            # 请求 DTO
│   │   ├── vo/             # 响应 VO
│   │   ├── config/         # 配置类（跨域、MyBatis-Plus、Security 等）
│   │   ├── common/         # 通用（Result、异常处理、工具类）
│   │   └── BlogApplication.java
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/         # XML（如需自定义 SQL）
└── prd.md                  # 本文档
```

---

## 五、开发里程碑

| 阶段 | 交付物 | 预估 |
|------|--------|------|
| **M1 基础设施** | 三工程初始化、数据库建表、统一响应与异常处理、JWT 鉴权 | 1 天 |
| **M2 后端核心 CRUD** | 文章/分类/标签/评论/配置 模块 REST API + springdoc/Swagger UI | 2 天 |
| **M3 管理后台前端** | 登录、布局、文章管理、分类标签、仪表盘 | 3 天 |
| **M4 用户端前端** | 首页、文章详情、关于我、评论 | 2 天 |
| **M5 联调与优化** | 接口联调、响应式适配、SEO、缓存（可选） | 2 天 |

---

## 六、安全设计要点

| 项目 | 方案 |
|------|------|
| 密码存储 | BCryptPasswordEncoder（Spring Security 提供） |
| 会话管理 | JWT（Access Token + Refresh Token，双 Token 方案） |
| 接口权限 | Spring Security 过滤 `/admin/**` 路径；公开 `/api/**` |
| CORS | 后端配置 CorsConfigurationSource，限制允许的 Origin |
| XSS | 前端展示时对用户输入转义；评论内容后端清洗 |
| SQL 注入 | MyBatis-Plus 参数化查询，严禁字符串拼接 SQL |
| 限流（可选） | 评论接口、登录接口加 IP 限流（Redis + RateLimiter） |

---

> **后续步骤**：确认本 PRD 内容后，即可按 M1 → M5 顺序启动开发。
