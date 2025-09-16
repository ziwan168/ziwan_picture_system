package com.ziwan.ziwanpicturebackend.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziwan.ziwanpicturebackend.annnotation.AuthCheck;
import com.ziwan.ziwanpicturebackend.common.BaseResponse;
import com.ziwan.ziwanpicturebackend.common.DeleteRequest;
import com.ziwan.ziwanpicturebackend.common.ResultUtils;
import com.ziwan.ziwanpicturebackend.constant.UserConstant;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import com.ziwan.ziwanpicturebackend.model.dto.picture.*;
import com.ziwan.ziwanpicturebackend.model.entity.Picture;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.enums.PictureReviewStatusEnum;
import com.ziwan.ziwanpicturebackend.model.vo.PictureTagCategory;
import com.ziwan.ziwanpicturebackend.model.vo.PictureVO;
import com.ziwan.ziwanpicturebackend.service.PictureService;
import com.ziwan.ziwanpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 上传图片通过url
     *
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTO_ERROR);
        boolean remove = pictureService.removeById(id);
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {


        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Picture picture = Picture.builder().build();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        pictureService.validPicture(picture);
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(oldPicture, loginUser);

        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 获取图片封装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        return ResultUtils.success(pictureVO);

    }

    /**
     * 分页获取图片列表
     *
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 最多展示20条, 防止前端传入的size过大
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAM_ERROR);
        // 只展示审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.REVIEW_PASS.getValue());

        // 分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    /**
     * 编辑（用户）
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest request) {

        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Picture picture = Picture.builder().build();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);

        Long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTO_ERROR);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 图片标签分类
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAM_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);

        return ResultUtils.success(true);
    }

    @PostMapping("load/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAM_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);

        return ResultUtils.success(pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser));
    }

}
