package com.example.weblog.jwt.service;

public interface AdminAuthService {

    /**
     * 登录认证，返回 Sa-Token token 字符串
     */
    String login(String username, String password);
}
