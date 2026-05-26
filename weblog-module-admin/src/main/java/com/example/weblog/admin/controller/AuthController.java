package com.example.weblog.admin.controller;

import com.example.weblog.admin.model.vo.auth.LoginReqVO;
import com.example.weblog.admin.model.vo.auth.LoginRspVO;
import com.example.weblog.common.aspect.ApiOperationLog;
import com.example.weblog.common.utils.Response;
import com.example.weblog.jwt.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService authService;

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录")
    public Response<LoginRspVO> login(@RequestBody @Valid LoginReqVO reqVO) {
        String token = authService.login(reqVO.getUsername(), reqVO.getPassword());
        return Response.success(new LoginRspVO(token));
    }
}
