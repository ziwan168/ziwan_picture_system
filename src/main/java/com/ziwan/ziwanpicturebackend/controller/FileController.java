package com.ziwan.ziwanpicturebackend.controller;


import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.ziwan.ziwanpicturebackend.annnotation.AuthCheck;
import com.ziwan.ziwanpicturebackend.common.BaseResponse;
import com.ziwan.ziwanpicturebackend.common.ResultUtils;
import com.ziwan.ziwanpicturebackend.constant.UserConstant;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;


    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {

        String originalFilename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", originalFilename);
        File file = null;

        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("file upload error, filepath = {} ", filePath, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {} ", filePath);
                }
            }
        }
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream objectContent = null;

        try {
            COSObject object = cosManager.getObject(filepath);
            objectContent = object.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(objectContent);

            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename" + filepath);

            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();

        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            if (objectContent != null) {
                objectContent.close();
            }
        }

    }

}
