package com.example.weblog.admin.utils;

import com.example.weblog.admin.config.MinioProperties;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.exception.BizException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Minio 文件上传工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 上传文件到 Minio，返回完整访问 URL。文件名使用 UUID 防重名。
     */
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = UUID.randomUUID().toString().replace("-", "") + ext;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String url = minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/" + objectName;
            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException(ResponseCodeEnum.FILE_UPLOAD_ERROR);
        }
    }
}
