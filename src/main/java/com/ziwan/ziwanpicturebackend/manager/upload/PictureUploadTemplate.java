package com.ziwan.ziwanpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.ziwan.ziwanpicturebackend.config.CosClientConfig;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.manager.CosManager;
import com.ziwan.ziwanpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;


/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片
     *
     * @param inputSource      文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        validPicture(inputSource);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);

        //解析结果并返回

        File file = null;

        try {
            file = File.createTempFile(uploadPath, null);

            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();


            return buildResult(imageInfo, uploadPath, originalFilename, file);


        } catch (IOException e) {
            log.error("图片上传存储对象失败 ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            //临时文件清理
            deleteTempFile(file);
        }


    }

    /**
     * 构建结果
     *
     * @param imageInfo
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @return
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String uploadPath, String originalFilename, File file) {
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
    }

    protected abstract void processFile(Object inputSource, File file) throws IOException;


    protected abstract String getOriginalFilename(Object inputSource);


    protected abstract void validPicture(Object inputSource);


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


}
