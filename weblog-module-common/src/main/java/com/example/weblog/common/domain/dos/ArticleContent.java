package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName t_article_content
 */
@TableName(value ="t_article_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleContent {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private String content;
}