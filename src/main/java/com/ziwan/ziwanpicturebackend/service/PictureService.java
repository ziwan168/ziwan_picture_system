package com.ziwan.ziwanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureQueryRequest;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureReviewRequest;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureUploadRequest;
import com.ziwan.ziwanpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
 * @author brave
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-09-10 23:07:53
 */
public interface PictureService extends IService<Picture> {


    /**
     * 校验
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);


    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 上传图片（批量）
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 返回创建成功的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);
}
