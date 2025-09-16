package com.ziwan.ziwanpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.ziwan.ziwanpicturebackend.common.ResultUtils;
import com.ziwan.ziwanpicturebackend.config.CosClientConfig;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import com.ziwan.ziwanpicturebackend.model.dto.file.UploadPictureResult;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;


@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);


        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);

        //解析结果并返回

        File file = null;

        try {
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();


            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();

            double picScale = NumberUtil.round((double) width / height, 2).doubleValue();

            return UploadPictureResult.builder()
                    .url(cosClientConfig.getHost() + "/" + uploadPath)
                    .picName(FileUtil.mainName(originalFilename))
                    .picSize(FileUtil.size(file))
                    .picWidth(width)
                    .picHeight(height)
                    .picScale(picScale)
                    .picFormat(imageInfo.getFormat())
                    .build();


        } catch (IOException e) {
            log.error("图片上传存储对象失败 ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            //临时文件清理
            deleteTempFile(file);
        }


    }


    private void validPicture(MultipartFile multipartFile) {
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


    /**
     * 删除临时文件
     *
     * @param file
     */
    public static void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("file delete error, filepath = {} ", file.getAbsoluteFile());
        }
    }


    /**
     * 通过url上传图片
     *
     * @param fileUrl          文件地址
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        //校验图片
        validPicture(fileUrl);


        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);

        //解析结果并返回

        File file = null;

        try {
            file = File.createTempFile(uploadPath, null);

            HttpUtil.downloadFile(fileUrl, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();


            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();

            double picScale = NumberUtil.round((double) width / height, 2).doubleValue();

            return UploadPictureResult.builder()
                    .url(cosClientConfig.getHost() + "/" + uploadPath)
                    .picName(FileUtil.mainName(originalFilename))
                    .picSize(FileUtil.size(file))
                    .picWidth(width)
                    .picHeight(height)
                    .picScale(picScale)
                    .picFormat(imageInfo.getFormat())
                    .build();


        } catch (IOException e) {
            log.error("图片上传存储对象失败 ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            //临时文件清理
            deleteTempFile(file);
        }


    }

    private void validPicture(String fileUrl) {
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
