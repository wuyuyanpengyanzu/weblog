package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName t_visitor_record
 */
@TableName(value ="t_visitor_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String visitor;

    private String ipAddress;

    private String ipRegion;

    private LocalDateTime visitTime;

    private Integer isNotify;
}