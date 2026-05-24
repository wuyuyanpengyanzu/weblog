package com.example.weblog.common.enums;

import com.example.weblog.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("10000", "系统异常"),
    PARAM_ERROR("10001", "参数错误"),
    USERNAME_OR_PASSWORD_ERROR("10006", "用户名或密码错误"),
    UNAUTHORIZED("10007", "未登录或Token已过期"),
    FORBIDDEN("10008", "无权限访问"),
    CATEGORY_NAME_DUPLICATED("20001", "分类名称已存在"),
    TAG_NAME_DUPLICATED("20002", "标签名称已存在"),
    ARTICLE_NOT_FOUND("20003", "文章不存在"),
    CATEGORY_NOT_FOUND("20004", "分类不存在"),
    TAG_NOT_FOUND("20005", "标签不存在"),
    FILE_UPLOAD_ERROR("30001", "文件上传失败"),
    ;

    private final String errorCode;
    private final String errorMessage;
}
