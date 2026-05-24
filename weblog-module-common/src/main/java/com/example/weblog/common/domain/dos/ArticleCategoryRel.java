package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文章-分类关联表，1:1 关系（UNIQUE INDEX on article_id） */
@TableName(value ="t_article_category_rel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCategoryRel {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long categoryId;
}