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
