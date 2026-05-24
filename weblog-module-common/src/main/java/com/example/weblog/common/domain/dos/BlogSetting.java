package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName t_blog_setting
 */
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

    private String avatar;

    private String githubHome;

    private String csdnHome;

    private String giteeHome;

    private String zhihuHome;
}