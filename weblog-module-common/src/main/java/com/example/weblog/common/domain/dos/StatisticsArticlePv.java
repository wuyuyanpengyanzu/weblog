package com.example.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 每日 PV 统计表，pvDate 唯一（UNIQUE INDEX），通过 upsert 累加 pvCount */
@TableName(value ="t_statistics_article_pv")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsArticlePv {
    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate pvDate; // 统计日期

    private Long pvCount; // 当日 PV 总量

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}