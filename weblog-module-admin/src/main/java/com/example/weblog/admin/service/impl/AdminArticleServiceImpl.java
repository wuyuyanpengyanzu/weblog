package com.example.weblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.admin.dao.AdminArticleDao;
import com.example.weblog.admin.model.vo.article.*;
import com.example.weblog.admin.model.vo.article.ArticleDetailRspVO.TagVO;
import com.example.weblog.admin.service.AdminArticleService;
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
        response.setRecords(records);
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