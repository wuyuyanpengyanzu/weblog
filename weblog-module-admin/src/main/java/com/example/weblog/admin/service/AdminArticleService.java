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
