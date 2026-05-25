# WeBlog 项目完全参考手册

> **用途：** 在新对话中注入本文档作为上下文，让任意 LLM 快速理解此项目并衔接工作。也作为你的个人学习路线图。
>
> **使用方法：** 开启新对话时，告诉 AI "请先阅读 `docs/WeBlog-Reference.md` 了解项目全貌"，然后将你的具体问题附在后面。

---

## 一、项目概览

WeBlog 是一款 **Spring Boot 2.6.3 + Vue 3.2 + Vite 4.3** 开发的前后端分离博客系统。

| 维度 | 说明 |
|------|------|
| 项目根目录 | `E:\WeBlog` |
| 后端入口 | `weblog-springboot/weblog-web/.../WeblogWebApplication.java` |
| 前端入口 | `weblog-vue3/src/main.js` |
| 数据库 | MySQL 5.7, 库名 `weblog` |
| 对象存储 | Minio（博客图片） |
| 端口 | 后端 8081，前端 Vite 开发服务器自动代理 `/api` 到后端 |

### 技术栈速览

**后端：** Spring Boot 2.6.3, MyBatis Plus 3.5.2, sa-token (认证授权), Minio 8.2.1, flexmark 0.62.2 (Markdown 渲染), Guava 18.0 (EventBus), ip2region (IP 归属地), HikariCP, p6spy (SQL 日志), MapStruct (DO↔VO 转换), Hibernate Validator

**前端：** Vue 3.2.47, Vue Router 4.1.6 (hash 模式), Vuex 4.0.2, Element Plus 2.3.3, ECharts 5.4.2, md-editor-v3 (Markdown 编辑器), WindiCSS (Tailwind), Axios, GSAP (动画), NProgress (进度条)

### 项目文件结构

```
E:\WeBlog\
├── sql/                          # 数据库初始化脚本
│   ├── schema.sql                # 11 张表结构
│   └── data.sql                  # 初始数据（admin/test 用户 + 博客设置）
├── weblog-springboot/            # 后端 Maven 父项目
│   ├── pom.xml                   # 父 POM，版本管理，模块声明
│   ├── weblog-module-common/     # 公共模块：DO、Mapper、AOP、异常、工具类
│   ├── weblog-module-jwt/        # JWT 认证模块：过滤器、Token 工具
│   ├── weblog-module-admin/      # 后台管理模块：Controller、Service、DAO
│   └── weblog-web/               # 启动入口 + 前台展示模块
│       └── src/main/resources/
│           ├── application.yaml          # 主配置（端口、JWT、Minio、ip2region 路径）
│           ├── application-dev.yaml      # 开发环境（MySQL 连接）
│           └── ip2region.xdb             # IP 地址库文件
└── weblog-vue3/                  # 前端 Vue 3 项目
    └── src/
        ├── main.js               # 入口：插件注册、路由守卫导入
        ├── App.vue               # 根组件
        ├── permission.js         # 路由守卫（Token 检查）
        ├── axios.js              # Axios 实例 + 拦截器（自动带 Token、401 处理）
        ├── router/index.js       # 路由表（后台 /admin/* + 前台 /*）
        ├── store/index.js        # Vuex（user、setting、menuWidth）
        ├── api/admin/            # 后台 API 调用（7 个模块）
        ├── api/frontend/         # 前台 API 调用（6 个模块）
        ├── pages/admin/          # 后台页面（6 个页面）
        ├── pages/frontend/       # 前台页面（7 个页面）
        ├── components/           # 共享组件（6 个）
        ├── layouts/              # 布局组件（admin.vue + 7 个子组件）
        └── composables/          # 组合函数（auth.js 认证工具、util.js）
```

---

## 二、架构核心

### 2.1 Maven 四模块依赖链（严格单向，无循环）

```
weblog-module-common     ← 基础设施：DO、Mapper、AOP、异常、工具、MyBatis Plus 配置
        ↑
weblog-module-jwt        ← sa-token 路由拦截配置
        ↑
weblog-module-admin      ← 后台业务：文章 CRUD、分类/标签管理、文件上传
        ↑
weblog-web               ← 启动入口 + 前台展示业务 + 所有配置文件
```

- `common` 不依赖任何兄弟模块
- `web` 依赖全部三个
- 启动类 `@ComponentScan({"com.quanxiaoha.weblog.*"})` 通配符扫描所有模块的 Bean

### 2.2 一个请求的完整生命周期

```
浏览器 → Vue Router (hash模式)
  → permission.js 路由守卫（检查 Token）
  → 页面组件 mounted() 调用 API
  → Axios 请求拦截器（自动附加 Authorization: Bearer <token>）
  → Vite 开发代理（/api → localhost:8081，去掉 /api 前缀）
  → SaTokenInterceptor（校验 Token，自动续期）
  → Controller（@ApiOperationLog AOP 记录日志 + 访客）
  → Service（业务逻辑，可能发布 EventBus 事件）
  → DAO → Mapper（MyBatis Plus）→ MySQL
  → Response.java 统一 JSON 包装
  → Axios 响应拦截器（检查 401）
  → 前端更新 DOM
```

### 2.3 数据库 11 张表及关系

```
t_user ─────────┐
  id, username,  │  (通过 username 字符串关联，非 FK)
  password       │
                 ├── t_user_role
                 │      id, username, role (ROLE_ADMIN / ROLE_VISITOR)
                 │
t_article ───────┤
  id, title,     │
  title_image,   │   t_article_content          ← 1:1（正文分离，提升列表查询性能）
  description,   │      id, article_id(FK), content(TEXT)
  read_num       │
                 │   t_article_category_rel      ← 1:1（UNIQUE INDEX on article_id）
                 │      id, article_id, category_id
                 │       ↑
                 │   t_category
                 │      id, name (UNIQUE)
                 │
                 │   t_article_tag_rel           ← M:N（无唯一约束，一篇文章多个标签）
                 │      id, article_id, tag_id
                 │       ↑
                 │   t_tag
                 │      id, name (UNIQUE)
                 │
t_statistics_article_pv    ← 每日 PV 聚合（pv_date UNIQUE, pv_count）
t_visitor_record           ← 访客记录（IP、归属地、访问时间、UA）
t_blog_setting             ← 博客设置单例表（只有一行，id=1）
```

**关键设计决策：**
- 文章和正文拆表：列表查询不需要加载 TEXT 大字段
- 文章和分类 1:1：每篇文章只能属于一个分类
- 文章和标签 M:N：通过关联表，无唯一约束
- 用户和角色不用 FK 约束，用 username 字符串关联

---

## 三、模块详解

### 3.1 登录认证

**核心链路：** `login.vue` → `POST /login` → `AuthController.login()` → BCrypt 手动验密 → `StpUtil.login(username)` → sa-token 生成 JWT Token → 返回 Token → 前端存 Cookie → 跳转仪表盘

#### 后端关键文件及职责

| 文件 | 路径 | 职责 |
|------|------|------|
| SaTokenConfig | `weblog-module-jwt/.../SaTokenConfig.java` | sa-token 路由拦截：`/admin/**` 需登录，放行 `/login` |
| AuthController | `weblog-module-jwt/.../controller/AuthController.java` | 登录接口：查用户 → BCrypt 验密 → `StpUtil.login()` → 返回 Token |
| PasswordEncoderConfig | `weblog-module-admin/.../config/PasswordEncoderConfig.java` | BCryptPasswordEncoder Bean |
| StpInterfaceImpl | `weblog-module-admin/.../config/StpInterfaceImpl.java` | sa-token 权限加载：从 DB 查询用户角色，每次请求自动调用 |
| ResultUtil | `weblog-module-jwt/.../utils/ResultUtil.java` | 向 HttpServletResponse 写 JSON 的工具 |

#### 前端关键文件

| 文件 | 路径 | 职责 |
|------|------|------|
| login.vue | `pages/admin/login.vue` | 登录表单 → API 调用 → 存 Token → 跳转 |
| axios.js | `src/axios.js` | 请求拦截器（自动带 Token Header）；响应拦截器（401 → 清 Token → 重定向登录页） |
| permission.js | `src/permission.js` | `router.beforeEach` 路由守卫：有 Token → 取用户信息；无 Token 且去 /admin → 重定向登录 |
| auth.js | `src/composables/auth.js` | Cookie 读写 Token（键名为 `Authorization`） |
| store/index.js | `src/store/index.js` | Vuex：user（用户信息）、setting（博客设置）、menuWidth（侧栏宽度） |

#### 设计要点

- **为什么选择 sa-token 而不是 Spring Security？** sa-token 更轻量：登录 `StpUtil.login()` 一句话，校验 `StpUtil.checkLogin()` 一行代码，无需理解过滤器链、AuthenticationManager、Provider 等概念
- **密码在哪校验？** 在 `AuthController.login()` 中手动调用 `BCryptPasswordEncoder.matches()`
- **Token 过期后怎么办？** sa-token 自动校验 Token 有效期，过期自动返回 401 → 前端 Axios 拦截器清 Cookie 并重定向
- **Token 不存角色**：角色由 `StpInterfaceImpl` 每次请求从 DB 实时加载。这样角色变更无需等 Token 过期

---

### 3.2 仪表盘

**三条 API：**

| API | 路径 | 返回 |
|-----|------|------|
| 统计概览 | `POST /admin/dashboard/article/statistics` | 4 个总数（文章、分类、标签、PV 总计） |
| 发布热图 | `POST /admin/dashboard/publishArticle/statistics` | `Map<String, Long>` 全年每天发文数 |
| PV 趋势 | `POST /admin/dashboard/pv/statistics` | 最近 7 天的日期数组 + PV 数量数组 |

#### PV 自增完整链路

```
用户读文章（前台 ArticleServiceImpl）
  → eventBus.post(new ArticleEvent(articleId, "PV_INCREASE"))
  → AdminEventListener.handleEvent()（Guava @Subscribe）
    → pvIncreaseAsyncTask.handle(articleId)（@Async 异步）
      → 1. articleDao.readNumIncrease(articleId)     -- t_article.read_num +1
      → 2. statisticsArticlePVDao.pvIncrease(currDate) -- t_statistics_article_pv upsert
```

**关键文件：** `PVIncreaseAsyncTask.java`, `AdminEventListener.java`, `EventBusConfig.java`

#### 访客记录

在 `ApiOperationLogAspect` 的 `@Before` 切面中实现：
1. `AgentRegionUtils.getIpAddress()` 从 `x-forwarded-for` → `Proxy-Client-IP` → `WL-Proxy-Client-IP` → `getRemoteAddr()` 链提取真实 IP
2. `AgentRegionUtils.getIpRegion()` 加载 `ip2region.xdb` 查询归属地
3. `ConcurrentHashMap` 日内 IP 去重
4. 每日 00:00:01 定时清空 Map

**关键文件：** `ApiOperationLogAspect.java`, `AgentRegionUtils.java`, `VisitorRecordDO.java`

#### 前端仪表盘

`pages/admin/index.vue` 包含：顶部 4 个统计卡片（`CountTo.vue` GSAP 动画）、文章发布日历热图（`ArticlePublishChart.vue`，6 个月滑动窗口）、PV 折线图（`PVChart.vue`）

---

### 3.3 文章管理（最核心模块）

#### 完整 API 列表

| API | 路径 | 权限 |
|-----|------|------|
| 发布文章 | `POST /admin/article/publish` | ROLE_ADMIN |
| 更新文章 | `POST /admin/article/update` | ROLE_ADMIN |
| 删除文章 | `POST /admin/article/delete` | ROLE_ADMIN |
| 文章分页 | `POST /admin/article/list` | 需登录 |
| 文章详情 | `POST /admin/article/detail` | 需登录 |
| 文件上传 | `POST /admin/file/upload` | ROLE_ADMIN |

#### 发布文章流程（事务内顺序）

```
@Transactional + TransactionTemplate.execute() {
  1. insert ArticleDO（拿自增 articleId）
  2. insert ArticleContentDO（articleId + content TEXT）
  3. insert ArticleCategoryRelDO（articleId + categoryId）
  4. handleTagBiz(articleId, tags):
     a. 查全部已有标签
     b. 新标签名 → 逐个 insert TagDO 拿自增 ID
     c. 全部关联行 → MyBaseMapper.insertBatchSomeColumn() 批量插入
}
```

#### 更新文章流程

与发布的核心区别：**先删后建**——删除旧的 `ArticleCategoryRelDO` 和 `ArticleTagRelDO`，再重新插入

#### 图片上传（Minio 后端上传，非前端直传）

```
前端 el-upload / md-editor-v3 @onUploadImg
  → POST /admin/file/upload（FormData）
  → MinioUtil.uploadFile() → 生成 UUID 文件名 → minioClient.putObject()
  → 返回 {endpoint}/{bucket}/{objectName} 完整 URL
```

题图上传和编辑器内嵌图片走同一个接口。

#### Markdown 渲染（后端渲染，非前端）

`MarkdownUtil.parse2Html()` 基于 flexmark：支持 GFM 表格、删除线、外链自动 `rel="nofollow"`（本站链接除外）、图片后插入 `<span class="image-caption">`、链接后插入外链 SVG 图标

#### 关键设计决策

- **正文表分离**：`t_article`（主信息）和 `t_article_content`（TEXT 正文）分开，列表查询不拉大字段
- **双重事务**：方法级 `@Transactional` + 方法内 `TransactionTemplate`
- **标签名而非 ID**：前端传标签名字符串列表，后端解析并自动创建不存在的标签
- **批量插入标签关联**：自定义 `MyBaseMapper.insertBatchSomeColumn()`，一次 SQL 插入所有关联行

**核心文件：** `AdminArticleServiceImpl.java`（最复杂，约 400 行）, `AdminArticleController.java`, `MinioUtil.java`, `MarkdownUtil.java`, `PublishArticleReqVO.java`, `UpdateArticleReqVO.java`

---

### 3.4 分类管理

| API | 路径 |
|-----|------|
| 新增分类 | `POST /admin/category/add` |
| 分页列表 | `POST /admin/category/list` |
| 删除分类 | `POST /admin/category/delete` |
| 全部（下拉用） | `POST /admin/category/select/list` |

**DAO/Mapper 双层模式：**
- `CategoryMapper`（common 模块）：纯 MyBatis-Plus BaseMapper，零自定义方法，可跨模块复用
- `AdminCategoryDao extends CategoryMapper`（admin 模块）：加自定义查询（selectAllCategory, selectTotalCount），封装 QueryWrapper

**核心文件：** `AdminCategoryController.java`, `AdminCategoryServiceImpl.java`, `AdminCategoryDaoImpl.java`

---

### 3.5 标签管理

与分类高度对称，区别在于：

| 差异点 | 标签 | 分类 |
|--------|------|------|
| 新增 | **批量**（`List<String>`） | 单个名称 |
| 搜索 | `POST /admin/tag/search`（大小写模糊匹配） | 无 |

**标签搜索 SQL：**
```sql
NAME like UPPER(CONCAT('%', key, '%')) OR NAME LIKE LOWER(CONCAT('%', key, '%'))
```

**核心文件：** `AdminTagController.java`, `AdminTagServiceImpl.java`, `AdminTagDaoImpl.java`

---

### 3.6 博客设置

**单例表模式**：`t_blog_setting` 只有一行（id=1），`saveOrUpdate` 维护。无创建、无删除、无列表。

**字段：** 博客名称、作者、头像、简介、GitHub/CSDN/Gitee/知乎链接

**API：** `POST /admin/blog/setting/detail`（公开），`POST /admin/blog/setting/update`（需 ROLE_ADMIN）

**消费点：** `AdminHeader.vue` 从 Vuex store 读取头像和用户名

**核心文件：** `AdminBlogSettingController.java`, `BlogSettingDO.java`, `AdminBlogSettingServiceImpl.java`

---

### 3.7 前台博客展示

**web 模块 vs admin 模块核心差异：**

| 维度 | weblog-web（前台） | weblog-module-admin（后台） |
|------|-------------------|----------------------------|
| URL 前缀 | `/index`, `/article`, `/category` 等 | `/admin/*` |
| 认证 | 无需认证 | sa-token 保护 |
| 读/写 | 只读展示 | 完整 CRUD |
| 特殊行为 | 读文章时发 EventBus PV 事件 | 无 |

#### 各页面对应的 API 和文件

| 页面 | API | 后端 Controller | 前端页面 |
|------|-----|-----------------|----------|
| 首页 | `POST /index/article/list` | `IndexController.java` | `frontend/index.vue` |
| 文章详情 | `POST /article/detail` | `ArticleController.java` | `frontend/article-detail.vue` |
| 分类列表 | `POST /category/list` | `CategoryController.java` | `frontend/category-list.vue` |
| 分类下文章 | `POST /category/article/list` | `CategoryController.java` | `frontend/category-article-list.vue` |
| 标签列表 | `POST /tag/list` | `TagController.java` | `frontend/tag-list.vue` |
| 标签下文章 | `POST /tag/article/list` | `TagController.java` | `frontend/tag-article-list.vue` |
| 归档 | `POST /archive/list` | `ArchiveController.java` | `frontend/archive-list.vue` |

#### 首页性能特征

一次首页请求做 5 次 DB 查询：文章分页 + 全量分类 + 分类关联 + 全量标签 + 标签关联。分类和标签表通常很小，但每次都全量查是可优化点。

#### 归档页实现

按月分组：查出所有文章 → 按 `createMonth`（yyyy-MM）→ `Collectors.groupingBy` → `TreeMap` 自定义降序比较器

#### 上一篇/下一篇

用 **ID 大小**判断（不是 create_time）：`WHERE id > currentId LIMIT 1` / `WHERE id < currentId LIMIT 1`。注意与列表按 `create_time` 排序的不一致。

#### MapStruct

web 模块两个转换器（`componentModel = "spring"`）：`ArticleConvert`（ArticleDO → VO，含日期格式化）、`BlogSettingConvert`。只处理字段映射，**不处理关联数据**（分类名、标签名在 Service 层手动 join）

---

### 3.8 公共能力

#### AOP 操作日志 + 访客记录

`@ApiOperationLog` 注解 → `ApiOperationLogAspect`：
- `@Before`：记录请求参数 + 访客登记（取 IP → ip2region 查归属地 → Map 去重 → 写 DB）
- `@Around`：计时 + 记录响应 + 耗时
- `@Scheduled(cron = "1 0 0 * * ?")`：每日零点清空 IP 去重 Map

#### Guava EventBus

- 使用同步 `EventBus`（非 `AsyncEventBus`），但订阅者内部调 `@Async` 方法
- `ArticleEvent` → `AdminEventListener.@Subscribe` → `PVIncreaseAsyncTask.handle()`
- 配置：`EventBusConfig.java` 注册 Listener

#### 全局异常处理

`@ControllerAdvice` 全局异常处理器捕获：

| 异常 | 处理 |
|------|------|
| `BizException` | 取 errorCode + errorMessage 返回 |
| `MethodArgumentNotValidException` | 遍历字段错误拼消息 |
| `Exception`（兜底） | 返回 SYSTEM_ERROR |

#### BaseExceptionInterface 错误码体系

接口定义 `getErrorCode()` + `getErrorMessage()`。`ResponseCodeEnum` 实现该接口。`BizException` 构造函数接受 `BaseExceptionInterface`，实现从枚举到异常的桥接。

#### 统一响应体

```json
// 成功
{"success": true, "errorCode": null, "message": null, "data": {...}}

// 分页成功
{"success": true, ..., "data": [...], "total": 100, "size": 10, "current": 1, "pages": 10}

// 失败
{"success": false, "errorCode": "10006", "message": "用户名或密码错误", "data": null}
```

#### MyBatis Plus 扩展

- `MyBaseMapper<T>` 继承 `BaseMapper<T>`，增加 `insertBatchSomeColumn()`
- `InsertBatchSqlInjector` 继承 `DefaultSqlInjector`，注入批量插入方法
- 原因：MyBatis Plus 默认不提供批量插入

#### 线程池

`ThreadPoolTaskExecutor`：核心 10，最大 20，队列 100，前缀 `"WeblogThreadPool-"`。由 `PVIncreaseAsyncTask` 的 `@Async` 使用。

---

## 四、完整文件索引

### 后端关键文件 (weblog-springboot)

#### weblog-module-common（基础设施层）

| 文件 | 路径缩写 | 作用 |
|------|---------|------|
| Response.java | `common/Response.java` | 统一 JSON 响应体 `{success, errorCode, message, data}` |
| PageResponse.java | `common/PageResponse.java` | 分页响应体（继承 Response，加 total/size/current/pages） |
| ApiOperationLog.java | `common/aspect/ApiOperationLog.java` | 自定义注解，标记需要日志的 API |
| ApiOperationLogAspect.java | `common/aspect/ApiOperationLogAspect.java` | AOP 切面：操作日志 + 访客记录 |
| EventBusConfig.java | `common/config/EventBusConfig.java` | Guava EventBus 注册 |
| InsertBatchSqlInjector.java | `common/config/InsertBatchSqlInjector.java` | 自定义 SQL 注入器（批量插入） |
| MyBaseMapper.java | `common/config/MyBaseMapper.java` | 自定义 Mapper 基类（加 insertBatchSomeColumn） |
| MybatisPlusConfig.java | `common/config/MybatisPlusConfig.java` | MyBatis Plus 配置（分页插件 + SQL 注入器） |
| Constants.java | `common/constant/Constants.java` | 全局常量 |
| EventEnum.java | `common/enums/EventEnum.java` | EventBus 事件类型枚举 |
| ResponseCodeEnum.java | `common/enums/ResponseCodeEnum.java` | 统一错误码枚举（实现 BaseExceptionInterface） |
| ArticleEvent.java | `common/eventbus/ArticleEvent.java` | 文章事件（携带 articleId 和 message） |
| EventListener.java | `common/eventbus/EventListener.java` | 事件监听接口 |
| BaseExceptionInterface.java | `common/exception/BaseExceptionInterface.java` | 错误码接口 |
| BizException.java | `common/exception/BizException.java` | 业务异常（接收 BaseExceptionInterface） |
| GlobalExceptionHandler.java | `common/exception/GlobalExceptionHandler.java` | `@ControllerAdvice` 全局异常处理 |
| NotAuthorizedException.java | `common/exception/NotAuthorizedException.java` | 未授权异常 |
| ResourceNotFoundException.java | `common/exception/ResourceNotFoundException.java` | 资源不存在异常 |
| AgentRegionUtils.java | `common/utils/AgentRegionUtils.java` | IP 提取 + ip2region 归属地查询 |
| ArticleDO.java | `common/domain/dos/ArticleDO.java` | 文章表 DO |
| ArticleContentDO.java | `common/domain/dos/ArticleContentDO.java` | 文章正文表 DO |
| ArticleCategoryRelDO.java | `common/domain/dos/ArticleCategoryRelDO.java` | 文章-分类关联 DO |
| ArticleTagRelDO.java | `common/domain/dos/ArticleTagRelDO.java` | 文章-标签关联 DO |
| CategoryDO.java | `common/domain/dos/CategoryDO.java` | 分类表 DO |
| TagDO.java | `common/domain/dos/TagDO.java` | 标签表 DO |
| BlogSettingDO.java | `common/domain/dos/BlogSettingDO.java` | 博客设置 DO |
| StatisticsArticlePVDO.java | `common/domain/dos/StatisticsArticlePVDO.java` | PV 统计 DO |
| VisitorRecordDO.java | `common/domain/dos/VisitorRecordDO.java` | 访客记录 DO |
| UserDO.java | `common/domain/dos/UserDO.java` | 用户表 DO |
| UserRoleDO.java | `common/domain/dos/UserRoleDO.java` | 用户角色 DO |
| ArticleMapper.java | `common/domain/mapper/ArticleMapper.java` | 文章 Mapper（含热图查询自定义 SQL） |
| 其他 Mapper | `common/domain/mapper/*.java` | 每张表对应一个 Mapper，均继承 BaseMapper 或 MyBaseMapper |

#### weblog-module-jwt（认证层）

| 文件 | 路径缩写 | 作用 |
|------|---------|------|
| SaTokenConfig.java | `jwt/SaTokenConfig.java` | sa-token 路由拦截：`/admin/**` 需登录，放行 `/login` |
| ResultUtil.java | `jwt/utils/ResultUtil.java` | 往 HttpServletResponse 写 JSON 的工具 |

#### weblog-module-admin（后台业务层）

| 文件 | 路径缩写 | 作用 |
|------|---------|------|
| WebSecurityConfig.java | `admin/config/WebSecurityConfig.java` | Security 配置：放行规则、STATELESS、注册 Filter |
| PasswordEncoderConfig.java | `admin/config/PasswordEncoderConfig.java` | BCrypt Bean |
| MinioConfig.java | `admin/config/MinioConfig.java` | MinioClient Bean |
| MinioProperties.java | `admin/config/MinioProperties.java` | Minio 配置属性 |
| ThreadPoolConfig.java | `admin/config/ThreadPoolConfig.java` | 线程池配置 |
| AdminArticleController.java | `admin/controller/AdminArticleController.java` | 文章管理 API（6 个端点） |
| AdminCategoryController.java | `admin/controller/AdminCategoryController.java` | 分类管理 API（4 个端点） |
| AdminTagController.java | `admin/controller/AdminTagController.java` | 标签管理 API（5 个端点） |
| AdminDashboardController.java | `admin/controller/AdminDashboardController.java` | 仪表盘 API（3 个端点） |
| AdminBlogSettingController.java | `admin/controller/AdminBlogSettingController.java` | 博客设置 API（2 个端点） |
| AdminFileController.java | `admin/controller/AdminFileController.java` | 文件上传 API |
| AdminUserController.java | `admin/controller/AdminUserController.java` | 用户信息 API |
| AuthController.java | `jwt/controller/AuthController.java` | 登录接口：验密 → `StpUtil.login()` → 返回 JWT Token |
| AdminArticleServiceImpl.java | `admin/service/impl/AdminArticleServiceImpl.java` | 文章核心业务（发布/更新/删除/分页，约 400 行，最复杂） |
| AdminDashboardServiceImpl.java | `admin/service/impl/AdminDashboardServiceImpl.java` | 仪表盘统计 |
| AdminFileServiceImpl.java | `admin/service/impl/AdminFileServiceImpl.java` | 文件上传业务 |
| AdminCategoryServiceImpl.java | `admin/service/impl/AdminCategoryServiceImpl.java` | 分类管理业务 |
| AdminTagServiceImpl.java | `admin/service/impl/AdminTagServiceImpl.java` | 标签管理业务 |
| AdminBlogSettingServiceImpl.java | `admin/service/impl/AdminBlogSettingServiceImpl.java` | 博客设置业务 |
| AdminUserServiceImpl.java | `admin/service/impl/AdminUserServiceImpl.java` | 用户信息/密码修改业务 |
| StpInterfaceImpl.java | `admin/config/StpInterfaceImpl.java` | sa-token 权限加载：从 DB 查询用户角色 |
| PVIncreaseAsyncTask.java | `admin/async/PVIncreaseAsyncTask.java` | @Async PV 自增 |
| AdminEventListener.java | `admin/eventbus/AdminEventListener.java` | Guava EventBus @Subscribe 监听 |
| MinioUtil.java | `admin/utils/MinioUtil.java` | Minio 文件上传/删除工具 |
| Admin*Dao.java | `admin/dao/*.java` | admin 侧 DAO 接口（继承 Mapper + 自定义方法） |
| Admin*DaoImpl.java | `admin/dao/impl/*.java` | admin 侧 DAO 实现（自定义 SQL） |

#### weblog-web（前台展示 + 启动入口）

| 文件 | 路径缩写 | 作用 |
|------|---------|------|
| WeblogWebApplication.java | `web/WeblogWebApplication.java` | @SpringBootApplication 启动入口 |
| IndexController.java | `web/controller/IndexController.java` | 前台首页 API |
| ArticleController.java | `web/controller/ArticleController.java` | 文章详情 API |
| CategoryController.java | `web/controller/CategoryController.java` | 前台分类 API |
| TagController.java | `web/controller/TagController.java` | 前台标签 API |
| ArchiveController.java | `web/controller/ArchiveController.java` | 归档 API |
| BlogSettingController.java | `web/controller/BlogSettingController.java` | 前台博客设置 API |
| ArticleServiceImpl.java | `web/service/impl/ArticleServiceImpl.java` | 前台文章服务（注：这里发 EventBus PV 事件） |
| ArchiveServiceImpl.java | `web/service/impl/ArchiveServiceImpl.java` | 归档服务（按月分组） |
| CategoryServiceImpl.java | `web/service/impl/CategoryServiceImpl.java` | 前台分类服务 |
| TagServiceImpl.java | `web/service/impl/TagServiceImpl.java` | 前台标签服务 |
| IndexServiceImpl.java | `web/service/impl/IndexServiceImpl.java` | 前台首页服务 |
| BlogSettingServiceImpl.java | `web/service/impl/BlogSettingServiceImpl.java` | 前台博客设置服务 |
| ArticleConvert.java | `web/convert/ArticleConvert.java` | MapStruct: ArticleDO → VO |
| BlogSettingConvert.java | `web/convert/BlogSettingConvert.java` | MapStruct: BlogSettingDO → VO |
| MarkdownUtil.java | `web/utils/MarkdownUtil.java` | flexmark Markdown → HTML 渲染 |

#### 配置文件

| 文件 | 路径 | 作用 |
|------|------|------|
| application.yaml | `weblog-web/src/main/resources/application.yaml` | 主配置：端口 8081, JWT, Minio, ip2region, Jackson |
| application-dev.yaml | `weblog-web/src/main/resources/application-dev.yaml` | 开发环境：MySQL 127.0.0.1:3306/weblog, root/123456, p6spy |
| ip2region.xdb | `weblog-web/src/main/resources/ip2region.xdb` | IP 地址库二进制文件 |
| pom.xml（父） | `weblog-springboot/pom.xml` | 父 POM：模块声明、版本管理 |
| schema.sql | `sql/schema.sql` | 11 张表 DDL |
| data.sql | `sql/data.sql` | admin/test 用户 + 博客设置初始数据 |

---

### 前端关键文件 (weblog-vue3/src)

| 文件 | 路径 | 作用 |
|------|------|------|
| main.js | `src/main.js` | 入口：插件注册顺序（store → router → Element Plus Icons → Viewer） |
| App.vue | `src/App.vue` | 根组件：`<el-config-provider>` + `<router-view>` |
| permission.js | `src/permission.js` | 路由守卫：检查 Token，获取用户信息 |
| axios.js | `src/axios.js` | Axios 实例：baseURL、请求拦截器（带 Token）、响应拦截器（401 处理） |
| router/index.js | `src/router/index.js` | 全部路由定义（后台 /admin/* + 前台 /*，hash 模式） |
| store/index.js | `src/store/index.js` | Vuex 状态：user, setting, menuWidth |
| composables/auth.js | `src/composables/auth.js` | Cookie 读写 Token |
| admin/login.vue | `pages/admin/login.vue` | 登录页 |
| admin/index.vue | `pages/admin/index.vue` | 仪表盘页（统计卡片 + 热图 + PV 图） |
| admin/article-list.vue | `pages/admin/article-list.vue` | 文章管理页（表格 + 搜索 + 抽屉式编辑器） |
| admin/category-list.vue | `pages/admin/category-list.vue` | 分类管理页 |
| admin/tag-list.vue | `pages/admin/tag-list.vue` | 标签管理页 |
| admin/blog-setting.vue | `pages/admin/blog-setting.vue` | 博客设置页 |
| frontend/index.vue | `pages/frontend/index.vue` | 前台首页（文章列表 + 侧栏） |
| frontend/article-detail.vue | `pages/frontend/article-detail.vue` | 文章详情页（后端渲染 HTML） |
| frontend/category-list.vue | `pages/frontend/category-list.vue` | 分类列表页 |
| frontend/category-article-list.vue | `pages/frontend/category-article-list.vue` | 某分类下文章列表 |
| frontend/tag-list.vue | `pages/frontend/tag-list.vue` | 标签列表页 |
| frontend/tag-article-list.vue | `pages/frontend/tag-article-list.vue` | 某标签下文章列表 |
| frontend/archive-list.vue | `pages/frontend/archive-list.vue` | 归档页 |
| components/MDEditor.vue | `components/MDEditor.vue` | Markdown 编辑器组件（基于 md-editor-v3） |
| components/FormDrawer.vue | `components/FormDrawer.vue` | 抽屉式表单（发布/编辑文章用） |
| components/ArticlePublishChart.vue | `components/ArticlePublishChart.vue` | 文章发布日历热图（ECharts） |
| components/PVChart.vue | `components/PVChart.vue` | PV 趋势折线图（ECharts） |
| components/CountTo.vue | `components/CountTo.vue` | 数字滚动动画（GSAP） |
| components/UserInfoCard.vue | `components/UserInfoCard.vue` | 用户信息卡片 |
| layouts/admin.vue | `layouts/admin.vue` | 后台管理布局（侧栏 + 顶栏 + 内容区） |
| layouts/components/AdminHeader.vue | `layouts/components/AdminHeader.vue` | 后台顶部栏（logo + 用户信息） |
| layouts/components/AdminMenu.vue | `layouts/components/AdminMenu.vue` | 后台侧栏菜单 |
| layouts/components/Header.vue | `layouts/components/Header.vue` | 前台顶部导航 |
| layouts/components/Footer.vue | `layouts/components/Footer.vue` | 前台页脚 |
| api/admin/*.js | `api/admin/*.js` | 后台 API 调用层（article/category/tag/dashboard/blogsetting/file/user） |
| api/frontend/*.js | `api/frontend/*.js` | 前台 API 调用层（index/article/category/tag/archive/blogsetting） |

---

## 五、长期学习与开发路线图

### 已完成阶段：代码走读（9 个模块）

- [x] 1. 项目骨架 — POM 结构、启动入口、配置文件、数据库表设计、前端外壳
- [x] 2. 登录认证 — sa-token JWT 模式（拦截器 + StpUtil）
- [x] 3. 仪表盘 — PV 统计、发布热图、访客记录
- [x] 4. 文章管理 — CRUD、多对多关联、Markdown、Minio 图片上传
- [x] 5. 分类管理 — DAO/Mapper 双层模式
- [x] 6. 标签管理 — 搜索、批量新增
- [x] 7. 博客设置 — 单例表模式
- [x] 8. 前台博客展示 — 首页、详情、归档、分类/标签页，前后台模块差异
- [x] 9. 公共能力 — AOP、EventBus、全局异常、统一响应、MyBatis Plus 扩展、线程池

### 下一阶段：实战开发（README 中的 TODO 功能）

按难度递增排列，建议按顺序做：

- [ ] **站内搜索** — 难度 ⭐⭐。在文章列表页加全文搜索（Elasticsearch 或 MySQL FULLTEXT），可涉及：新表/索引设计、前台搜索 API、后台搜索 API、前端搜索框组件
- [ ] **博客评论** — 难度 ⭐⭐⭐。涉及：新表 `t_comment`、评论 CRUD API、评论审核（后台）、前台评论列表 + 提交表单、嵌套回复（可选）
- [ ] **知识库 Wiki** — 难度 ⭐⭐⭐⭐。全新的独立功能模块，可涉及：Wiki 页面树形结构、Markdown 编辑、版本历史

### 进阶阶段：优化与增强

- [ ] 首页性能优化 — 缓存全量分类/标签列表，减少 DB 查询（当前每次首页请求 5 次查询）
- [ ] 前台 Markdown 渲染缓存 — 文章正文渲染后的 HTML 缓存到 Redis
- [ ] 图片上传改为前端直传 Minio — 后端只返回 pre-signed URL，减轻后端带宽
- [ ] 上一篇/下一篇用 create_time 排序 — 修复当前 ID 排序与列表排序不一致的问题
- [ ] 添加分类编辑功能 — 目前只有增删查，没有改
- [ ] 文章草稿功能 — 添加文章状态（草稿/已发布）
- [ ] RSS 订阅 — 生成 RSS feed
- [ ] SEO 优化 — 前后端 SSR 或预渲染

---

## 六、如何向其他大模型提问

将以下模板附在你的问题前面：

```
我在学习一个名为 WeBlog 的开源博客项目。以下是项目概况，请基于此回答我的问题。

**项目路径：** E:\WeBlog
**后端：** Spring Boot 2.6.3 + MyBatis Plus 3.5.2 + sa-token + Minio + Guava EventBus
**前端：** Vue 3.2 + Vite 4.3 + Element Plus + ECharts + Vuex + Vue Router (hash)
**数据库：** MySQL 5.7
**模块结构：** common(基础设施) → jwt(认证) → admin(后台业务) → web(启动入口+前台展示)

**我想问：** [你的具体问题]
```

### 按模块提问示例

```
# 问认证流程
请帮我追踪 WeBlog 的完整登录认证链路。
关键文件：
- SaTokenConfig.java (路由拦截配置)
- AuthController.java (登录接口 + BCrypt 验密 + StpUtil.login())
- StpInterfaceImpl.java (DB 角色加载)
- login.vue → axios.js → permission.js (前端流程)

# 问文章管理
请帮我理解 WeBlog 的文章发布流程。
关键文件：AdminArticleServiceImpl.java 的 publishArticle() 方法。
涉及多表：t_article, t_article_content, t_article_category_rel, t_article_tag_rel。
图片上传走 Minio，关键文件：AdminFileController.java, MinioUtil.java。

# 问仪表盘
请解释 WeBlog 的 PV 自增和访客记录机制。
PV 自增链路：ArticleServiceImpl(web) → EventBus → AdminEventListener → PVIncreaseAsyncTask(@Async)
访客记录：ApiOperationLogAspect(@Before) + ip2region + ConcurrentHashMap 日内去重

# 问前台 vs 后台
WeBlog 有两个模块处理请求：weblog-web(前台) 和 weblog-module-admin(后台)。
它们有什么不同？为什么不能合并？请从认证、数据返回、模块职责三个维度分析。

# 问某个具体文件
请阅读并分析 E:\WeBlog\weblog-springboot\weblog-module-admin\...\AdminArticleServiceImpl.java
这个文件是文章管理的核心业务类。请解释它的主要方法和事务管理方式。
```

---

## 附录：数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS weblog DEFAULT CHARSET utf8mb4;"

# 2. 导入表结构
mysql -u root -p weblog < sql/schema.sql

# 3. 导入初始数据
mysql -u root -p weblog < sql/data.sql
```

**初始账户：**
- 管理员：`admin` / `admin`（ROLE_ADMIN）
- 游客：`test` / `test`（ROLE_VISITOR）
