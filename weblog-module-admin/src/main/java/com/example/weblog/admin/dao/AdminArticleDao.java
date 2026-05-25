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
