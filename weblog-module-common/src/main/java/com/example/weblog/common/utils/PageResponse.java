package com.example.weblog.common.utils;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页响应体。T 表示列表中单条数据的类型，{@code records} 存列表。
 * 继承 Response 复用 success/errorCode/message，父类 data 留空不用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends Response<Object> {

    private List<T> records; // 分页数据列表

    private long total;
    private long size;
    private long current;
    private long pages;
}
