package com.ziwan.ziwanpicturebackend.manager.upload;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 通过url上传图片
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;

        HttpUtil.downloadFile(fileUrl, file);

    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        //校验图片
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAM_ERROR, "文件不能为空");

        //校验文件地址
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件格式错误");
        }

        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAM_ERROR, "仅支持HTTP 或 HTTPS 协议的文件地址");

        //获取文件头
        HttpResponse response = null;
        try {
            // 尝试 HEAD 请求
            response = HttpUtil.createRequest(Method.HEAD, fileUrl)
                    .header("User-Agent", "Mozilla/5.0 (compatible; PictureValidator/1.0)")
                    .timeout(5000)
                    .execute();

            // HEAD 不支持则退回 GET
            if (!response.isOk()) {
                response.close();
                response = HttpUtil.createRequest(Method.GET, fileUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .header("Range", "bytes=0-2048") // 只取前 2KB
                        .timeout(8000)
                        .execute();
            }

            String header = response.header("Content-Type");
            final List<String> ALLOW_FORMAT_LIST = Arrays.asList(
                    "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/jpg"
            );

            // === ✨ 宽容判断逻辑 ===
            boolean isImage = StrUtil.isNotBlank(header)
                    && (header.toLowerCase().contains("image/") || ALLOW_FORMAT_LIST.contains(header.toLowerCase()));

            // 如果 header 不可靠，尝试从文件 URL 后缀判断
            if (!isImage) {
                String lowerUrl = fileUrl.toLowerCase();
                for (String ext : Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")) {
                    if (lowerUrl.endsWith(ext)) {
                        isImage = true;
                        break;
                    }
                }
            }

            ThrowUtils.throwIf(!isImage, ErrorCode.PARAM_ERROR, "文件格式错误（仅支持 JPG/PNG/GIF/BMP/WEBP）");

            // === 校验文件大小 ===
            long contentLength = response.contentLength();
            final long ONE_MB = 1024 * 1024;
            if (contentLength > 0) {
                ThrowUtils.throwIf(contentLength > 3 * ONE_MB, ErrorCode.PARAM_ERROR, "文件大小不能超过 3MB");
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无法访问文件地址：" + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}




