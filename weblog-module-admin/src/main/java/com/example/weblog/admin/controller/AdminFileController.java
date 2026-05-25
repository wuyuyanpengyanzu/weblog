package com.example.weblog.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.weblog.admin.utils.MinioUtil;
import com.example.weblog.common.aspect.ApiOperationLog;
import com.example.weblog.common.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 Controller（Minio 后端上传）
 */
@RestController
@RequiredArgsConstructor
public class AdminFileController {

    private final MinioUtil minioUtil;

    @PostMapping("/admin/file/upload")
    @ApiOperationLog(description = "文件上传")
    @SaCheckRole("ROLE_ADMIN")
    public Response<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = minioUtil.uploadFile(file);
        return Response.success(url);
    }
}
