# 阶段三：后台文章管理 设计文档

> 基于 WeBlog-Reference.md 和 2026-05-22-weblog-design.md，在阶段一（骨架）和阶段二（sa-token 认证）基础上实现。

## 一、范围

本阶段实现后台文章管理的完整链路：6 个文章 API + 1 个文件上传 API + Minio 工具 + Markdown 渲染工具。

## 二、文件清单（约 14 个新文件）

### weblog-module-admin

| 文件 | 位置 | 职责 |
|------|------|------|
| AdminArticleController | `admin/controller/` | 6 个文章 API 端点 |
| AdminFileController | `admin/controller/` | 1 个文件上传端点 |
| AdminArticleService | `admin/service/` | 文章业务接口 |
| AdminArticleServiceImpl | `admin/service/impl/` | 核心业务：发布/更新/删除/分页/详情 |
| AdminArticleDao | `admin/dao/` | 封装复杂查询（分页 JOIN） |
| PublishArticleReqVO | `admin/model/vo/article/` | 发布请求 VO，含校验注解 |
| UpdateArticleReqVO | `admin/model/vo/article/` | 更新请求 VO |
| DeleteArticleReqVO | `admin/model/vo/article/` | 删除请求 VO |
| ArticleListReqVO | `admin/model/vo/article/` | 分页查询请求 VO |
| ArticleDetailRspVO | `admin/model/vo/article/` | 详情/列表响应 VO |
| MinioUtil | `admin/utils/` | Minio 上传工具 |

### weblog-web

| 文件 | 位置 | 职责 |
|------|------|------|
| MarkdownUtil | `web/utils/` | flexmark Markdown 转 HTML |

## 三、API 设计

| API | 路径 | 权限 | 说明 |
|-----|------|------|------|
| 发布文章 | `POST /admin/article/publish` | `@SaCheckRole("ROLE_ADMIN")` | 写 4 张表 |
| 更新文章 | `POST /admin/article/update` | `@SaCheckRole("ROLE_ADMIN")` | 先删关联再重建 |
| 删除文章 | `POST /admin/article/delete` | `@SaCheckRole("ROLE_ADMIN")` | 逻辑删除 |
| 文章分页 | `POST /admin/article/list` | 需登录 | 多表 JOIN 分页 |
| 文章详情 | `POST /admin/article/detail` | 需登录 | 含分类名+标签名 |
| 文件上传 | `POST /admin/file/upload` | `@SaCheckRole("ROLE_ADMIN")` | FormData → Minio |

## 四、发布文章流程

```
@Transactional(rollbackFor = Exception.class) {
  1. insert Article — 拿自增 articleId
  2. insert ArticleContent — articleId + content TEXT
  3. insert ArticleCategoryRel — articleId + categoryId
  4. 标签处理:
     a. 查全部已有标签
     b. 新标签名 → 逐个 insert Tag 拿自增 ID
     c. 全部关联行 → MyBaseMapper.insertBatchSomeColumn() 批量插入 ArticleTagRel
}
```

## 五、更新文章流程

与发布的区别：先删除旧的 ArticleCategoryRel 和 ArticleTagRel，再重新插入。正文直接 UPDATE 而非 INSERT。

## 六、关键设计决策

- **包名：** `com.example.weblog`（与现有代码一致）
- **DO 命名：** 无 "DO" 后缀（`Article` 而非 `ArticleDO`）
- **日期类型：** `java.time.LocalDateTime` / `java.time.LocalDate`
- **事务：** 方法级 `@Transactional(rollbackFor = Exception.class)`，不用双重事务
- **VO 层：** 每个 API 独立 VO，含 Hibernate Validator 校验注解
- **DAO 层：** `AdminArticleDao extends ArticleMapper`，封装自定义分页查询
- **分页查询：** JOIN `t_article` + `t_article_category_rel` + `t_category` + `t_article_tag_rel` + `t_tag`，在 DAO 中用自定义 SQL
- **Markdown 渲染：** 放在 web 模块，使用 flexmark-all
- **文件上传：** Minio 后端上传（非前端直传），返回完整 URL
