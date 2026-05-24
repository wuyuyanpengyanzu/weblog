package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 博客设置表，单例模式（只有一行，id=1），通过 saveOrUpdate 维护 */
@TableName(value ="t_blog_setting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogSetting {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String blogName;

    private String author;

    private String introduction;

    private String avatar; // 头像 URL

    private String githubHome;

    private String csdnHome;

    private String giteeHome;

    private String zhihuHome;
}