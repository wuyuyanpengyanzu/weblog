package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文章-标签关联表，M:N 关系（无唯一约束，一篇文章可对应多个标签） */
@TableName(value ="t_article_tag_rel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTagRel {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long tagId;
}