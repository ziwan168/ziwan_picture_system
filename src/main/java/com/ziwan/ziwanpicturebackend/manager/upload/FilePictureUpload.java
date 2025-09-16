package com.ziwan.ziwanpicturebackend.manager.upload;


import cn.hutool.core.io.FileUtil;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAM_ERROR, "文件不能为空");

        //校验文件大小
        long fileSize = multipartFile.getSize();

        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 3 * ONE_MB, ErrorCode.PARAM_ERROR, "文件大小不能超过3MB");

        //校验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //获取所有允许的后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAM_ERROR, "文件格式错误");


    }
}
