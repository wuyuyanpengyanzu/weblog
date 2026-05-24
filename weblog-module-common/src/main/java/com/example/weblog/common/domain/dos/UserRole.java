package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户角色表，通过 username 字符串与 t_user 关联（非 FK），角色值如 ROLE_ADMIN / ROLE_VISITOR */
@TableName(value ="t_user_role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userName;

    private String role; // ROLE_ADMIN 或 ROLE_VISITOR

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}