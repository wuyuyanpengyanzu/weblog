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
