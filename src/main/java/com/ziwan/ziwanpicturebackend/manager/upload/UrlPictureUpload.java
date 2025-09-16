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
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (!response.isOk()) {
                return;
            }

            String header = response.header("Content-Type");
            if (StrUtil.isNotBlank(header)) {
                final List<String> ALLOW_FORMAT_LIST =
                        Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(header.toLowerCase()), ErrorCode.PARAM_ERROR, "文件格式错误");

            }

            //校验文件大小
            long contentLength = response.contentLength();
            final long ONE_MB = 1024 * 1024;
            ThrowUtils.throwIf(contentLength > 3 * ONE_MB, ErrorCode.PARAM_ERROR, "文件大小不能超过3MB");
        } finally {
            if (response != null) {
                response.close();
            }

        }
    }

}




