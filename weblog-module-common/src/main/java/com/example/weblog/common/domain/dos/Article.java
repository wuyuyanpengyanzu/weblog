package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文章主表 */
@TableName(value ="t_article")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String titleImage; // 题图 URL

    private String description; // 文章摘要

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    private Integer readNum; // 阅读次数
}