package com.example.weblog.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weblog.common.domain.dos.UserRole;
import com.example.weblog.common.domain.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserName, loginId.toString()));
        return userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
    }
}
