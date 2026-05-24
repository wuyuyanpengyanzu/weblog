package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 访客记录表，由 AOP 切面在 API 请求时自动写入，ConcurrentHashMap 同 IP 日内去重 */
@TableName(value ="t_visitor_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String visitor; // 访客标识，默认 "agent"

    private String ipAddress;

    private String ipRegion; // IP 归属地，由 ip2region 查询

    private LocalDateTime visitTime;

    private Integer isNotify; // 是否已通知（预留字段）
}