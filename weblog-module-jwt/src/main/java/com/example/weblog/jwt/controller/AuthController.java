package com.example.weblog.jwt.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weblog.common.domain.dos.User;
import com.example.weblog.common.domain.mapper.UserMapper;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Response<Map<String, String>> login(@RequestBody Map<String, String> loginData) {

        String username = loginData.get("username");
        String password = loginData.get("password");

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserName, username));
        if (user == null) {
            return Response.fail(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorCode(),
                    ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorMessage());
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Response.fail(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorCode(),
                    ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorMessage());
        }

        StpUtil.login(user.getUserName());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Response.success(Map.of("token", tokenInfo.getTokenValue()));
    }
}
