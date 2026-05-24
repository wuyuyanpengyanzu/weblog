# 阶段三：后台文章管理 实现计划

> **供执行代理使用：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐个实现。步骤使用 checkbox（`- [ ]`）语法追踪。

**目标：** 实现后台文章管理完整链路——6 个文章 API（发布/更新/删除/分页/详情）+ 1 个文件上传 API + Minio 工具 + Markdown 渲染工具。

**架构：** VO（admin/model/vo/article/）→ Controller → Service → DAO → Mapper（common）。@Transactional 保证多表写入原子性。DAO 封装复杂 JOIN 分页查询。

**依赖：** 阶段一（骨架）+ 阶段二（sa-token 认证）已完成，`mvn compile` 通过。

---

### 任务 1：创建 5 个 VO 类

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/model/vo/article/PublishArticleReqVO.java`
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/model/vo/article/UpdateArticleReqVO.java`
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/model/vo/article/DeleteArticleReqVO.java`
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/model/vo/article/ArticleListReqVO.java`
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/model/vo/article/ArticleDetailRspVO.java`

- [ ] **步骤 1：创建目录**

```bash
mkdir -p "E:/git_codes/Web-Log/weblog-module-admin/src/main/java/com/example/weblog/admin/model/vo/article"
```

- [ ] **步骤 2：创建 PublishArticleReqVO**

```java
package com.example.weblog.admin.model.vo.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发布文章请求 VO
 */
@Data
public class PublishArticleReqVO {

    @NotBlank(message = "文章标题不能为空")
    private String title;

    @NotBlank(message = "文章正文不能为空")
    private String content; // Markdown 格式正文

    @NotNull(message = "文章分类不能为空")
    private Long categoryId;

    @NotEmpty(message = "文章标签不能为空")
    private List<String> tags; // 标签名称列表，不传 ID

    private String titleImage; // 题图 URL

    private String description; // 文章摘要
}
```

- [ ] **步骤 3：创建 UpdateArticleReqVO**

```java
package com.example.weblog.admin.model.vo.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新文章请求 VO
 */
@Data
public class UpdateArticleReqVO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @NotBlank(message = "文章标题不能为空")
    private String title;

    @NotBlank(message = "文章正文不能为空")
    private String content; // Markdown 格式正文

    @NotNull(message = "文章分类不能为空")
    private Long categoryId;

    @NotEmpty(message = "文章标签不能为空")
    private List<String> tags; // 标签名称列表

    private String titleImage;

    private String description;
}
```

- [ ] **步骤 4：创建 DeleteArticleReqVO**

```java
package com.example.weblog.admin.model.vo.article;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除文章请求 VO（也用于文章详情请求，共用 articleId 字段）
 */
@Data
public class DeleteArticleReqVO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
```

- [ ] **步骤 5：创建 ArticleListReqVO**

```java
package com.example.weblog.admin.model.vo.article;

import lombok.Data;

/**
 * 文章分页查询请求 VO
 */
@Data
public class ArticleListReqVO {

    private Long current = 1L; // 当前页码

    private Long size = 10L; // 每页条数

    private String searchWord; // 标题搜索关键词（可选）
}
```

- [ ] **步骤 6：创建 ArticleDetailRspVO**

```java
package com.example.weblog.admin.model.vo.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情响应 VO（也用于分页列表的每行数据，列表场景下 content 为空）
 */
@Data
public class ArticleDetailRspVO {

    private Long id;

    private String title;

    private String titleImage;

    private String description;

    private String content; // Markdown 正文，列表查询时不填充

    private Long categoryId;

    private String categoryName;

    private List<TagVO> tags;

    private Integer readNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 标签简要信息（仅含 id 和名称）
     */
    @Data
    public static class TagVO {
        private Long id;
        private String name;
    }
}
```

- [ ] **步骤 7：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-module-admin -q
```

预期：BUILD SUCCESS

---

### 任务 2：创建 AdminArticleDao（自定义分页查询）

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/dao/AdminArticleDao.java`
- 新建：`weblog-module-admin/src/main/resources/mapper/AdminArticleDao.xml`

- [ ] **步骤 1：创建目录**

```bash
mkdir -p "E:/git_codes/Web-Log/weblog-module-admin/src/main/java/com/example/weblog/admin/dao"
mkdir -p "E:/git_codes/Web-Log/weblog-module-admin/src/main/resources/mapper"
```

- [ ] **步骤 2：创建 AdminArticleDao**

```java
package com.example.weblog.admin.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.admin.model.vo.article.ArticleDetailRspVO;
import com.example.weblog.common.domain.mapper.ArticleMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章 DAO — 继承 common 模块的 ArticleMapper，扩展自定义分页查询。
 * SQL 映射文件：resources/mapper/AdminArticleDao.xml
 */
public interface AdminArticleDao extends ArticleMapper {

    /** 分页查询文章列表，LEFT JOIN 分类表拿分类名称 */
    IPage<ArticleDetailRspVO> selectArticlePage(Page<ArticleDetailRspVO> page, @Param("searchWord") String searchWord);
}
```

配套的 XML 映射文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.weblog.admin.dao.AdminArticleDao">

    <select id="selectArticlePage" resultType="com.example.weblog.admin.model.vo.article.ArticleDetailRspVO">
        SELECT a.id, a.title, a.title_image AS titleImage, a.description,
               a.create_time AS createTime, a.update_time AS updateTime, a.read_num AS readNum,
               c.id AS categoryId, c.name AS categoryName
        FROM t_article a
        LEFT JOIN t_article_category_rel acr ON a.id = acr.article_id
        LEFT JOIN t_category c ON acr.category_id = c.id
        WHERE a.is_deleted = 0
        <if test="searchWord != null and searchWord != ''">
            AND a.title LIKE CONCAT('%', #{searchWord}, '%')
        </if>
        ORDER BY a.create_time DESC
    </select>

</mapper>
```

> MyBatis Plus 默认扫描 `classpath*:/mapper/**/*.xml`，无需额外配置 `mybatis-plus.mapper-locations`。

- [ ] **步骤 3：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-module-admin -q
```

预期：BUILD SUCCESS

---

### 任务 3：创建 AdminArticleService 接口和实现

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/service/AdminArticleService.java`
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/service/impl/AdminArticleServiceImpl.java`

- [ ] **步骤 1：创建目录**

```bash
mkdir -p "E:/git_codes/Web-Log/weblog-module-admin/src/main/java/com/example/weblog/admin/service/impl"
```

- [ ] **步骤 2：创建 AdminArticleService 接口**

```java
package com.example.weblog.admin.service;

import com.example.weblog.admin.model.vo.article.*;
import com.example.weblog.common.utils.PageResponse;
import com.example.weblog.common.utils.Response;

public interface AdminArticleService {

    Response<?> publishArticle(PublishArticleReqVO reqVO);

    Response<?> updateArticle(UpdateArticleReqVO reqVO);

    Response<?> deleteArticle(DeleteArticleReqVO reqVO);

    PageResponse<ArticleDetailRspVO> listArticles(ArticleListReqVO reqVO);

    Response<ArticleDetailRspVO> getArticleDetail(DeleteArticleReqVO reqVO);
}
```

- [ ] **步骤 3：创建 AdminArticleServiceImpl**

```java
package com.example.weblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.admin.dao.AdminArticleDao;
import com.example.weblog.admin.model.vo.article.*;
import com.example.weblog.admin.model.vo.article.ArticleDetailRspVO.TagVO;
import com.example.weblog.common.domain.dos.*;
import com.example.weblog.common.domain.mapper.*;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.exception.BizException;
import com.example.weblog.common.utils.PageResponse;
import com.example.weblog.common.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminArticleServiceImpl implements AdminArticleService {

    private final AdminArticleDao adminArticleDao;
    private final ArticleContentMapper articleContentMapper;
    private final ArticleCategoryRelMapper articleCategoryRelMapper;
    private final ArticleTagRelMapper articleTagRelMapper;
    private final TagMapper tagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> publishArticle(PublishArticleReqVO reqVO) {
        // 1. 插入文章主表
        Article article = Article.builder()
                .title(reqVO.getTitle())
                .titleImage(reqVO.getTitleImage())
                .description(reqVO.getDescription())
                .readNum(0)
                .build();
        adminArticleDao.insert(article);
        Long articleId = article.getId();

        // 2. 插入正文
        ArticleContent content = ArticleContent.builder()
                .articleId(articleId)
                .content(reqVO.getContent())
                .build();
        articleContentMapper.insert(content);

        // 3. 插入分类关联
        ArticleCategoryRel categoryRel = ArticleCategoryRel.builder()
                .articleId(articleId)
                .categoryId(reqVO.getCategoryId())
                .build();
        articleCategoryRelMapper.insert(categoryRel);

        // 4. 处理标签
        handleTagBiz(articleId, reqVO.getTags());

        return Response.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> updateArticle(UpdateArticleReqVO reqVO) {
        Long articleId = reqVO.getArticleId();

        Article article = adminArticleDao.selectById(articleId);
        if (article == null) {
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_FOUND);
        }

        // 更新文章主表
        article.setTitle(reqVO.getTitle());
        article.setTitleImage(reqVO.getTitleImage());
        article.setDescription(reqVO.getDescription());
        adminArticleDao.updateById(article);

        // 更新正文：先查是否存在
        ArticleContent content = articleContentMapper.selectOne(
                new LambdaQueryWrapper<ArticleContent>().eq(ArticleContent::getArticleId, articleId));
        if (content != null) {
            content.setContent(reqVO.getContent());
            articleContentMapper.updateById(content);
        } else {
            content = ArticleContent.builder()
                    .articleId(articleId)
                    .content(reqVO.getContent())
                    .build();
            articleContentMapper.insert(content);
        }

        // 删除旧分类关联，插入新分类关联
        articleCategoryRelMapper.delete(
                new LambdaQueryWrapper<ArticleCategoryRel>().eq(ArticleCategoryRel::getArticleId, articleId));
        ArticleCategoryRel categoryRel = ArticleCategoryRel.builder()
                .articleId(articleId)
                .categoryId(reqVO.getCategoryId())
                .build();
        articleCategoryRelMapper.insert(categoryRel);

        // 删除旧标签关联，重新处理
        articleTagRelMapper.delete(
                new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, articleId));
        handleTagBiz(articleId, reqVO.getTags());

        return Response.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> deleteArticle(DeleteArticleReqVO reqVO) {
        Article article = adminArticleDao.selectById(reqVO.getArticleId());
        if (article == null) {
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_FOUND);
        }
        adminArticleDao.deleteById(reqVO.getArticleId());
        return Response.success(null);
    }

    @Override
    public PageResponse<ArticleDetailRspVO> listArticles(ArticleListReqVO reqVO) {
        Page<ArticleDetailRspVO> page = new Page<>(reqVO.getCurrent(), reqVO.getSize());
        IPage<ArticleDetailRspVO> result = adminArticleDao.selectArticlePage(page, reqVO.getSearchWord());

        // 补充每个文章的标签列表
        List<ArticleDetailRspVO> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> articleIds = records.stream().map(ArticleDetailRspVO::getId).collect(Collectors.toList());
            // 查标签关联
            List<ArticleTagRel> tagRels = articleTagRelMapper.selectList(
                    new LambdaQueryWrapper<ArticleTagRel>().in(ArticleTagRel::getArticleId, articleIds));
            if (!tagRels.isEmpty()) {
                List<Long> tagIds = tagRels.stream().map(ArticleTagRel::getTagId).distinct().collect(Collectors.toList());
                List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                Map<Long, String> tagIdNameMap = tags.stream().collect(Collectors.toMap(Tag::getId, Tag::getName));
                Map<Long, List<TagVO>> articleTagsMap = tagRels.stream()
                        .collect(Collectors.groupingBy(ArticleTagRel::getArticleId,
                                Collectors.mapping(rel -> {
                                    TagVO vo = new TagVO();
                                    vo.setId(rel.getTagId());
                                    vo.setName(tagIdNameMap.get(rel.getTagId()));
                                    return vo;
                                }, Collectors.toList())));
                records.forEach(r -> r.setTags(articleTagsMap.get(r.getId())));
            }
        }

        PageResponse<ArticleDetailRspVO> response = new PageResponse<>();
        response.setData(records);
        response.setTotal(result.getTotal());
        response.setSize(result.getSize());
        response.setCurrent(result.getCurrent());
        response.setPages(result.getPages());
        return response;
    }

    @Override
    public Response<ArticleDetailRspVO> getArticleDetail(DeleteArticleReqVO reqVO) {
        Article article = adminArticleDao.selectById(reqVO.getArticleId());
        if (article == null) {
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_FOUND);
        }

        ArticleDetailRspVO vo = new ArticleDetailRspVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setTitleImage(article.getTitleImage());
        vo.setDescription(article.getDescription());
        vo.setReadNum(article.getReadNum());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        // 查正文
        ArticleContent content = articleContentMapper.selectOne(
                new LambdaQueryWrapper<ArticleContent>().eq(ArticleContent::getArticleId, article.getId()));
        if (content != null) {
            vo.setContent(content.getContent());
        }

        // 查分类
        ArticleCategoryRel categoryRel = articleCategoryRelMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategoryRel>().eq(ArticleCategoryRel::getArticleId, article.getId()));
        if (categoryRel != null) {
            vo.setCategoryId(categoryRel.getCategoryId());
        }

        // 查标签
        List<ArticleTagRel> tagRels = articleTagRelMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, article.getId()));
        if (!tagRels.isEmpty()) {
            List<Long> tagIds = tagRels.stream().map(ArticleTagRel::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            List<TagVO> tagVOs = tags.stream().map(tag -> {
                TagVO tagVO = new TagVO();
                tagVO.setId(tag.getId());
                tagVO.setName(tag.getName());
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOs);
        }

        return Response.success(vo);
    }

    /**
     * 处理标签业务：新标签入库，批量插入文章-标签关联
     */
    private void handleTagBiz(Long articleId, List<String> tagNames) {
        // 查全部已有标签
        List<Tag> allTags = tagMapper.selectList(null);
        Map<String, Long> nameIdMap = allTags.stream()
                .collect(Collectors.toMap(Tag::getName, Tag::getId));

        // 区分新旧标签：新标签入库拿 ID
        List<Long> tagIds = new ArrayList<>();
        for (String name : tagNames) {
            Long tagId = nameIdMap.get(name);
            if (tagId == null) {
                Tag newTag = Tag.builder().name(name).build();
                tagMapper.insert(newTag);
                tagId = newTag.getId();
            }
            tagIds.add(tagId);
        }

        // 批量插入关联
        List<ArticleTagRel> rels = tagIds.stream()
                .map(tagId -> ArticleTagRel.builder().articleId(articleId).tagId(tagId).build())
                .collect(Collectors.toList());
        articleTagRelMapper.insertBatchSomeColumn(rels);
    }
}
```

- [ ] **步骤 4：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-module-admin -q
```

预期：BUILD SUCCESS

---

### 任务 4：创建 AdminArticleController

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/controller/AdminArticleController.java`

- [ ] **步骤 1：创建目录**

```bash
mkdir -p "E:/git_codes/Web-Log/weblog-module-admin/src/main/java/com/example/weblog/admin/controller"
```

- [ ] **步骤 2：创建 AdminArticleController**

```java
package com.example.weblog.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.weblog.admin.model.vo.article.*;
import com.example.weblog.admin.service.AdminArticleService;
import com.example.weblog.common.aspect.ApiOperationLog;
import com.example.weblog.common.utils.PageResponse;
import com.example.weblog.common.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台文章管理 Controller
 */
@RestController
@RequiredArgsConstructor
public class AdminArticleController {

    private final AdminArticleService articleService;

    @PostMapping("/admin/article/publish")
    @ApiOperationLog(description = "发布文章")
    @SaCheckRole("ROLE_ADMIN")
    public Response<?> publishArticle(@RequestBody @Valid PublishArticleReqVO reqVO) {
        return articleService.publishArticle(reqVO);
    }

    @PostMapping("/admin/article/update")
    @ApiOperationLog(description = "更新文章")
    @SaCheckRole("ROLE_ADMIN")
    public Response<?> updateArticle(@RequestBody @Valid UpdateArticleReqVO reqVO) {
        return articleService.updateArticle(reqVO);
    }

    @PostMapping("/admin/article/delete")
    @ApiOperationLog(description = "删除文章")
    @SaCheckRole("ROLE_ADMIN")
    public Response<?> deleteArticle(@RequestBody @Valid DeleteArticleReqVO reqVO) {
        return articleService.deleteArticle(reqVO);
    }

    // 仅需登录，不需要 ROLE_ADMIN
    @PostMapping("/admin/article/list")
    @ApiOperationLog(description = "文章分页列表")
    public PageResponse<ArticleDetailRspVO> listArticles(@RequestBody ArticleListReqVO reqVO) {
        return articleService.listArticles(reqVO);
    }

    // 仅需登录，不需要 ROLE_ADMIN
    @PostMapping("/admin/article/detail")
    @ApiOperationLog(description = "文章详情")
    public Response<ArticleDetailRspVO> getArticleDetail(@RequestBody @Valid DeleteArticleReqVO reqVO) {
        return articleService.getArticleDetail(reqVO);
    }
}
```

- [ ] **步骤 3：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-module-admin -q
```

预期：BUILD SUCCESS

---

### 任务 5：创建 MinioUtil（文件上传工具）

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/utils/MinioUtil.java`

- [ ] **步骤 1：创建目录**

```bash
mkdir -p "E:/git_codes/Web-Log/weblog-module-admin/src/main/java/com/example/weblog/admin/utils"
```

- [ ] **步骤 2：创建 MinioUtil**

```java
package com.example.weblog.admin.utils;

import com.example.weblog.admin.config.MinioProperties;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.exception.BizException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Minio 文件上传工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 上传文件到 Minio，返回完整访问 URL。文件名使用 UUID 防重名。
     */
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = UUID.randomUUID().toString().replace("-", "") + ext;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String url = minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/" + objectName;
            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException(ResponseCodeEnum.FILE_UPLOAD_ERROR);
        }
    }
}
```

- [ ] **步骤 3：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-module-admin -q
```

预期：BUILD SUCCESS

---

### 任务 6：创建 AdminFileController

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/controller/AdminFileController.java`

- [ ] **步骤 1：创建 AdminFileController**

```java
package com.example.weblog.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.weblog.admin.utils.MinioUtil;
import com.example.weblog.common.aspect.ApiOperationLog;
import com.example.weblog.common.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 Controller（Minio 后端上传）
 */
@RestController
@RequiredArgsConstructor
public class AdminFileController {

    private final MinioUtil minioUtil;

    @PostMapping("/admin/file/upload")
    @ApiOperationLog(description = "文件上传")
    @SaCheckRole("ROLE_ADMIN")
    public Response<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = minioUtil.uploadFile(file);
        return Response.success(url);
    }
}
```

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-module-admin -q
```

预期：BUILD SUCCESS

---

### 任务 7：创建 MarkdownUtil（flexmark 渲染）

**涉及文件：**
- 新建：`weblog-web/src/main/java/com/example/weblog/web/utils/MarkdownUtil.java`

- [ ] **步骤 1：创建目录**

```bash
mkdir -p "E:/git_codes/Web-Log/weblog-web/src/main/java/com/example/weblog/web/utils"
```

- [ ] **步骤 2：创建 MarkdownUtil**

```java
package com.example.weblog.web.utils;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Markdown → HTML 渲染工具，基于 flexmark。解析器静态初始化，线程安全。
 */
public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
                com.vladsch.flexmark.ext.tables.TablesExtension.create(),           // GFM 表格
                com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create() // 删除线
        ));
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    /** 将 Markdown 文本渲染为 HTML */
    public static String parse2Html(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        return RENDERER.render(PARSER.parse(markdown));
    }
}
```

- [ ] **步骤 3：验证编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -pl weblog-web -q
```

预期：BUILD SUCCESS

---

### 任务 8：全量编译验证

- [ ] **步骤 1：全量编译**

```bash
cd "E:/git_codes/Web-Log" && mvn compile -q
```

预期：全部 4 个模块 BUILD SUCCESS

---

**阶段三完成。** 后台文章管理全链路已打通：

- 6 个文章 API：发布（4 表事务写入）、更新（先删关联再重建）、删除（逻辑删除）、分页（JOIN 查询 + 标签补充）、详情
- 1 个文件上传 API：FormData → Minio → 返回 URL
- MinioUtil 和 MarkdownUtil 工具类就绪
- 权限控制：发布/更新/删除需 ROLE_ADMIN，分页/详情需登录
