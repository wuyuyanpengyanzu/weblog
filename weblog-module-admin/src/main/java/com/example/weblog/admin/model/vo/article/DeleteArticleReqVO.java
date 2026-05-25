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
