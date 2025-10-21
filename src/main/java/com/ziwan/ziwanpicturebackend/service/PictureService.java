package com.ziwan.ziwanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziwan.ziwanpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ziwan.ziwanpicturebackend.model.dto.picture.*;
import com.ziwan.ziwanpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author brave
 * &#064;description  针对表【picture(图片)】的数据库操作Service
 * &#064;createDate  2025-09-10 23:07:53
 */
public interface PictureService extends IService<Picture> {


    /**
     * 校验
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     *
     * @param inputSource 输入源
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 登录的用户
     * @return 图片 vo
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取图片包装类（单条）
     *
     * @param picture 图片
     * @param request 请求
     * @return 图片 vo
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);


    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage 图片查询请求
     * @param request 请求
     * @return 图片 vo
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser 登录的用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture  图片
     * @param loginUser 登录的用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 上传图片（批量）
     *
     * @param pictureUploadByBatchRequest 图片上传请求
     * @param loginUser 登录的用户
     * @return 返回创建成功的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);


    /**
     * 删除图片
     *
     * @param oldPicture 旧图片
     */
    void clearPicture(Picture oldPicture);

    /**
     * 检查空间图片权限
     *
     * @param loginUser 登录的用户
     * @param picture  图片
     */
    void checkPictureAuth(User loginUser,Picture picture);

    /**
     * 删除图片
     *
     * @param id  id
     * @param loginUser 登录的用户
     */
    void deletePicture(Long id, User loginUser);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest 图片编辑请求
     * @param loginUser 登录的用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 按照颜色相似度查询图片
     *
     * @param spaceId   spaceId
     * @param picColor  颜色
     * @param loginUser 登录的用户
     * @return 图片 vo 结合
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);


    /**
     * 批量更新
     *
     * @param pictureEditByBatchRequest pictureEditByBatchRequest
     * @param loginUser                 登录的用户
     */
    void batchEditPictureMetadata(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);


    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

}
