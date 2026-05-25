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