package com.example.weblog.jwt.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weblog.common.domain.dos.User;
import com.example.weblog.common.domain.mapper.UserMapper;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.exception.BizException;
import com.example.weblog.jwt.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserName, username));
        if (user == null) {
            throw new BizException(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        StpUtil.login(user.getUserName());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return tokenInfo.getTokenValue();
    }
}
