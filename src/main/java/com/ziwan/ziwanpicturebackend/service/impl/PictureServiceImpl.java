package com.ziwan.ziwanpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import com.ziwan.ziwanpicturebackend.manager.FileManager;
import com.ziwan.ziwanpicturebackend.manager.upload.FilePictureUpload;
import com.ziwan.ziwanpicturebackend.manager.upload.PictureUploadTemplate;
import com.ziwan.ziwanpicturebackend.manager.upload.UrlPictureUpload;
import com.ziwan.ziwanpicturebackend.model.dto.file.UploadPictureResult;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureQueryRequest;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureReviewRequest;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.ziwan.ziwanpicturebackend.model.dto.picture.PictureUploadRequest;
import com.ziwan.ziwanpicturebackend.model.entity.Picture;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.enums.PictureReviewStatusEnum;
import com.ziwan.ziwanpicturebackend.model.vo.PictureVO;
import com.ziwan.ziwanpicturebackend.model.vo.UserVO;
import com.ziwan.ziwanpicturebackend.service.PictureService;
import com.ziwan.ziwanpicturebackend.mapper.PictureMapper;
import com.ziwan.ziwanpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author brave
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-09-10 23:07:53
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private FilePictureUpload filePictureUpload;


    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAM_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAM_ERROR, "id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAM_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAM_ERROR, "简介过长");
        }

    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTO_ERROR);

        Long pictureId = null;
        if (pictureUploadRequest != null && pictureUploadRequest.getId() != null) {
            pictureId = pictureUploadRequest.getId();
        }
        if (pictureId != null) {

            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTO_ERROR);
            }

//            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
//            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //上传图片
        String uploadPathPrefix = String.format("picture/%s", loginUser.getId());
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);


        Picture picture = Picture.builder()
                .url(uploadPictureResult.getUrl())
                .name(uploadPictureResult.getPicName())
                .picSize(uploadPictureResult.getPicSize())
                .picWidth(uploadPictureResult.getPicWidth())
                .picHeight(uploadPictureResult.getPicHeight())
                .picScale(uploadPictureResult.getPicScale())
                .picFormat(uploadPictureResult.getPicFormat())
                .userId(loginUser.getId())
                .build();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getNamePrefix())) {
            picture.setName(pictureUploadRequest.getNamePrefix());

        }


        this.fillReviewParams(picture, loginUser);


        //插入数据库
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean save = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "图片上传失败");

        return PictureVO.objToVo(picture);
    }


    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = pictureVO.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(),
                picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());

        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());

        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));

        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }


        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();


        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(
                    qw -> qw.like("name", searchText)
                            .or()
                            .like("introduction", searchText)

            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);


        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"),
                sortField);


        return queryWrapper;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1. 参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.NOT_FOUND_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf(id == null ||
                        reviewStatusEnum == null ||
                        PictureReviewStatusEnum.REVIEW_WAITING.equals(reviewStatusEnum),
                ErrorCode.PARAM_ERROR);
        //2.判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //3.校验审核状态是否重复
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片已审核");
        }
        //4.数据库操作
        Picture updatePicture = Picture.builder().build();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEW_PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员审核通过");
            picture.setReviewTime(new Date());
        } else {
            //非管理员
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEW_WAITING.getValue());
        }

    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();


        ThrowUtils.throwIf(count >= 40, ErrorCode.PARAM_ERROR, "上传数量不能大于40");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }


        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);


        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("图片获取失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片获取失败");
        }
        Elements div = document.getElementsByClass("dgControl");
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取元素失败");
        }
        Elements elements = div.select("img.ming");
        int uploadCount = 0;
        for (Element element : elements) {
            String fileUrl = element.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("图片地址{}为空,当前为第 {} 个", fileUrl, uploadCount);
                continue;
            }
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setNamePrefix(namePrefix + (uploadCount + 1));

            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功,当前为第 {} 个,id = {}", uploadCount, pictureVO.getId());
                uploadCount++;

            } catch (Exception e) {
                log.error("图片上传失败,当前为第 {} 个,url = {}", uploadCount, fileUrl);
                continue;

            }
            if (uploadCount >= count) {
                break;
            }


        }

        return uploadCount;


    }


}




