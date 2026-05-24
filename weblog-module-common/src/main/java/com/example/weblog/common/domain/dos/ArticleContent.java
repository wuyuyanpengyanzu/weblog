package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文章正文表，与 t_article 1:1，分离大字段以提升列表查询性能 */
@TableName(value ="t_article_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleContent {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private String content; // Markdown 正文（TEXT 类型）
}