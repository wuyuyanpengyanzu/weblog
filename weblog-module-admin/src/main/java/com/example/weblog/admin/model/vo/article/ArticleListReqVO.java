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
