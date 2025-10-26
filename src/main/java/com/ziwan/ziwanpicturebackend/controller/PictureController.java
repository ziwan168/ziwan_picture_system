package com.ziwan.ziwanpicturebackend.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziwan.ziwanpicturebackend.annnotation.AuthCheck;
import com.ziwan.ziwanpicturebackend.api.aliyunai.ALiYunAiApi;
import com.ziwan.ziwanpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ziwan.ziwanpicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.ziwan.ziwanpicturebackend.api.imagesearch.ImageSearchApiFacade;
import com.ziwan.ziwanpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.ziwan.ziwanpicturebackend.common.BaseResponse;
import com.ziwan.ziwanpicturebackend.common.DeleteRequest;
import com.ziwan.ziwanpicturebackend.common.ResultUtils;
import com.ziwan.ziwanpicturebackend.constant.UserConstant;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import com.ziwan.ziwanpicturebackend.manager.auth.SpaceUserAuthManager;
import com.ziwan.ziwanpicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.ziwan.ziwanpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.ziwan.ziwanpicturebackend.model.dto.picture.*;
import com.ziwan.ziwanpicturebackend.model.dto.space.SpaceLevel;
import com.ziwan.ziwanpicturebackend.model.entity.Picture;
import com.ziwan.ziwanpicturebackend.model.entity.Space;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.enums.PictureReviewStatusEnum;
import com.ziwan.ziwanpicturebackend.model.enums.SpaceLevelEnum;
import com.ziwan.ziwanpicturebackend.model.vo.PictureTagCategory;
import com.ziwan.ziwanpicturebackend.model.vo.PictureVO;
import com.ziwan.ziwanpicturebackend.service.PictureService;
import com.ziwan.ziwanpicturebackend.service.SpaceService;
import com.ziwan.ziwanpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;


    @Resource
    private SpaceService spaceService;
    @Resource
    private ALiYunAiApi aLiYunAiApi;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


    /**
     * 上传图片
     *
     * @param multipartFile        图片
     * @param pictureUploadRequest 图片上传信息
     * @param request              请求
     * @return 图片封装类
     */
    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
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
     * @param pictureUploadRequest 图片上传信息
     * @param request              请求
     * @return 图片封装类
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest 删除的图片信息
     * @param request       请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest 修改的图片信息
     * @param request              请求
     * @return 修改结果
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
     * @param id      图片id
     * @param request 请求
     * @return 图片封装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //空间权限校验
        Long spaceId = picture.getSpaceId();
        Space space = null;
        User loginUser = null;
        if (spaceId != null) {
            loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        }
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        pictureVO.setPermissionList(permissionList);
        return ResultUtils.success(pictureVO);

    }

    /**
     * 分页获取图片列表
     *
     * @param pictureQueryRequest 查询条件
     * @return 图片列表
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
     * @param pictureQueryRequest 查询条件
     * @param request             请求
     * @return 图片列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 最多展示20条, 防止前端传入的size过大
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAM_ERROR);

        //空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 公共空间
            // 只展示审核通过的图片
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.REVIEW_PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTO_ERROR, "无此空间权限");

        }
        // 分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    /**
     * 分页获取图片列表（封装类）
     *
     * @param pictureQueryRequest 搜索条件
     * @param request             请求
     * @return 图片列表
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {

        Page<PictureVO> pictureVOPage = pictureService.listPictureVOByPageWithCache(pictureQueryRequest, request);
        return ResultUtils.success(pictureVOPage);

    }


    /**
     * 编辑（用户）
     *
     * @param pictureEditRequest 修改的图片信息
     * @param request            请求
     * @return 修改结果
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest request) {

        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 图片标签分类
     *
     * @return 图片标签分类
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
     * @param pictureReviewRequest 图片审核请求
     * @param request              请求
     * @return 响应
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
        int count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);

        return ResultUtils.success(count);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevels = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum ->
                        new SpaceLevel(
                                spaceLevelEnum.getValue(),
                                spaceLevelEnum.getText(),
                                spaceLevelEnum.getMaxSize(),
                                spaceLevelEnum.getMaxCount()
                        )).toList();

        return ResultUtils.success(spaceLevels);

    }

    //以图搜图
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest,
                                                                        HttpServletRequest request) {

        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAM_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAM_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> imageSearchResults;
        if (oldPicture.getThumbnailUrl() != null) {
            imageSearchResults = ImageSearchApiFacade.searchImage(oldPicture.getThumbnailUrl());
        } else {
            imageSearchResults = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
        }

        return ResultUtils.success(imageSearchResults);


    }

    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAM_ERROR);
        User loginUser = userService.getLoginUser(request);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();

        List<PictureVO> pictureListVO = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureListVO);

    }

    /**
     * 批量修改图片元数据
     *
     * @param pictureEditByBatchRequest 批量修改图片元数据请求
     * @param request                   请求
     * @return 响应
     */
    @PostMapping("/batch/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> batchEditPictureMetadata(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
                                                          HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAM_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.batchEditPictureMetadata(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAM_ERROR);
        GetOutPaintingTaskResponse task = aLiYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }


}
