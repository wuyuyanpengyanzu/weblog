# WeBlog 设计文档

> 基于 WeBlog-Reference.md，将原 Spring Boot 2.6.3 项目升级到 Spring Boot 3.4.5 重新实现。

## 一、模块结构

```
Web_Blog/                          # 父 POM（版本管理）
├── weblog-module-common/          # 基础设施：DO、Mapper、AOP、异常、工具、MyBatis Plus 配置
├── weblog-module-jwt/             # sa-token 认证：路由拦截配置
├── weblog-module-admin/           # 后台业务：Controller、Service、DAO、Minio
├── weblog-web/                    # 启动入口 + 前台展示 + 配置文件
└── sql/                           # 数据库脚本（已有 schema.sql + data.sql）
```

依赖方向：`common ← jwt ← admin ← web`（无循环）

- common 不依赖任何兄弟模块
- web 依赖全部三个
- 启动类使用 `@ComponentScan({"com.quanxiaoha.weblog.*"})` 通配符扫描

## 二、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.5 | 核心框架 |
| MyBatis Plus | 3.5.9 | ORM + 分页 |
| sa-token | 1.42.0 | 认证授权（JWT 模式） |
| Minio | 8.5.11 | 对象存储（博客图片） |
| flexmark | 0.64.8 | Markdown 转 HTML |
| Guava | 33.4.0 | EventBus |
| ip2region | 2.7.0 | IP 归属地查询 |
| MapStruct | 1.6.3 | DO ↔ VO 转换 |
| Hibernate Validator | 8.x (随 Boot) | 参数校验 |
| p6spy | 1.10.0 | SQL 日志 |
| HikariCP | (随 Boot) | 数据库连接池 |

## 三、Spring Boot 3.x 适配要点

- **javax.* → jakarta.***：Servlet、Validation、Persistence 全部改包名
- **sa-token**：使用 `sa-token-spring-boot3-starter`，天然兼容 Spring Boot 3.x；JWT 模式通过 `token-style: jwt` 配置启用
- **p6spy**：使用 `p6spy-spring-boot-starter`，通过 application.yaml 配置
- **MyBatis Plus 3.5.9**：分页插件配置方式保持不变，兼容 Spring Boot 3

## 四、数据库（11 张表，已有 DDL）

沿用现有 `sql/schema.sql` 和 `sql/data.sql`，11 张表关系：

```
t_user ─────────┐
  id, username,  │  (通过 username 字符串关联，非 FK)
  password       │
                 ├── t_user_role
                 │      id, username, role (ROLE_ADMIN / ROLE_VISITOR)
                 │
t_article ───────┤
  id, title,     │
  title_image,   │   t_article_content          ← 1:1（正文分离）
  description,   │      id, article_id, content(TEXT)
  read_num,      │
  create_time,   │   t_article_category_rel      ← 1:1（UNIQUE INDEX on article_id）
  update_time,   │      id, article_id, category_id
  is_deleted     │       ↑
                 │   t_category
                 │      id, name (UNIQUE)
                 │
                 │   t_article_tag_rel           ← M:N（无唯一约束）
                 │      id, article_id, tag_id
                 │       ↑
                 │   t_tag
                 │      id, name (UNIQUE)
                 │
t_statistics_article_pv    ← 每日 PV 聚合
t_visitor_record           ← 访客记录
t_blog_setting             ← 博客设置单例表
```

## 五、实现阶段

### 阶段一：项目骨架
- 父 POM + 四模块 POM（Spring Boot 3.4.5 + 全部依赖版本管理）
- 11 个 DO 实体类
- 11 个 Mapper 接口（继承 BaseMapper 或 MyBaseMapper）
- 公共类：Response、PageResponse、BizException、GlobalExceptionHandler、ResponseCodeEnum、Constants
- AOP：ApiOperationLog 注解 + 切面骨架
- MyBatis Plus 扩展：MyBaseMapper + InsertBatchSqlInjector
- 启动类 + application.yaml + application-dev.yaml
- **目标：编译通过，Spring Boot 启动成功**

### 阶段二：sa-token 认证
- sa-token-spring-boot3-starter + sa-token-jwt 依赖
- SaTokenConfig 路由拦截（`/admin/**` 需登录，放行 `/login`）
- AuthController（登录接口：BCrypt 验密 → `StpUtil.login()` → 返回 JWT Token）
- StpInterfaceImpl（DB 角色加载）
- application.yaml 配置 JWT 模式
- **目标：POST /login 调通，返回 JWT Token**

### 阶段三：后台文章管理
- AdminArticleController + AdminArticleServiceImpl
- 发布/更新/删除/分页/详情 6 个 API
- 文章-正文分离、文章-分类 1:1、文章-标签 M:N
- MinioUtil + AdminFileController（图片上传）
- MarkdownUtil（flexmark 渲染）
- **目标：6 个文章 API 全部可用**

### 阶段四：后台分类 & 标签管理
- AdminCategoryController + Service（增/删/查 4 个 API）
- AdminTagController + Service（批量新增/搜索/删除/分页 5 个 API）
- DAO/Mapper 双层模式
- **目标：9 个管理 API**

### 阶段五：仪表盘
- AdminDashboardController + Service（3 个 API）
- 统计概览（文章数、分类数、标签数、总 PV）
- 发布热图（全年每天发文数 Map）
- PV 趋势（最近 7 天）
- EventBus + PVIncreaseAsyncTask（@Async PV 自增）
- ip2region 访客记录（ApiOperationLogAspect 中实现）
- **目标：3 个仪表盘 API**

### 阶段六：博客设置
- AdminBlogSettingController + Service
- 单例表查询和更新
- **目标：2 个设置 API**

### 阶段七：前台博客展示
- IndexController + ArticleController + CategoryController + TagController + ArchiveController
- 首页、文章详情、分类/标签浏览、归档、上一篇/下一篇
- MapStruct DO↔VO 转换
- 前端 Service 中发 EventBus PV 事件
- **目标：~8 个前台 API**

### 阶段八：Vue 3 前端
- main.js + App.vue + router + store + axios
- permission.js 路由守卫
- 后台 6 页面：login、index（仪表盘）、article-list、category-list、tag-list、blog-setting
- 前台 7 页面：index、article-detail、category-list、category-article-list、tag-list、tag-article-list、archive-list
- 布局组件 + 共享组件
- API 调用层（admin 7 模块 + frontend 6 模块）
- **目标：完整可交互前端**

## 六、关键架构决策（沿用原设计）

- 文章和正文拆表：列表查询不加载 TEXT 大字段
- 文章和分类 1:1（UNIQUE INDEX on article_id）
- 文章和标签 M:N（关联表无唯一约束）
- 用户和角色无 FK 约束，用 username 字符串关联
- Token 不存角色，每次请求从 DB 重新加载
- 登录逻辑在 Controller 中手动验密，Token 校验由 sa-token 拦截器自动完成
- 双重事务：方法级 @Transactional + TransactionTemplate
- 批量插入标签关联：自定义 MyBaseMapper.insertBatchSomeColumn()
- 博客设置单例表：只有一行 id=1
- PV 自增：同步 EventBus → @Async 异步执行
- 访客记录在 AOP 切面中实现，ConcurrentHashMap 日内去重
