package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName t_user_role
 */
@TableName(value ="t_user_role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userName;

    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}