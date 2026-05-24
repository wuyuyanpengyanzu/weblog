package com.example.weblog.common.utils;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends Response<List<T>> {

    private long total;
    private long size;
    private long current;
    private long pages;
}
