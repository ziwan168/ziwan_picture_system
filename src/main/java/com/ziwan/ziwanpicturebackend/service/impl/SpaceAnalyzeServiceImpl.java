package com.ziwan.ziwanpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import com.ziwan.ziwanpicturebackend.model.dto.space.analyze.*;
import com.ziwan.ziwanpicturebackend.model.entity.Picture;
import com.ziwan.ziwanpicturebackend.model.entity.Space;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.enums.TimeDimensionEnum;
import com.ziwan.ziwanpicturebackend.model.vo.space.*;
import com.ziwan.ziwanpicturebackend.service.PictureService;
import com.ziwan.ziwanpicturebackend.service.SpaceAnalyzeService;
import com.ziwan.ziwanpicturebackend.service.SpaceService;
import com.ziwan.ziwanpicturebackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author brave
 * &#064;createDate  2025-10-15 18:40:52
 */
@Service
@AllArgsConstructor
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {
    private final UserService userService;
    private final SpaceService spaceService;
    private final PictureService pictureService;


    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {

        boolean queryPublic = spaceUsageAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceUsageAnalyzeRequest.isQueryAll();
        // 判断权限：全空间、公开空间，仅管理员
        if (queryAll || queryPublic) {
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> pictureObjects = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjects.stream().mapToLong(obj -> (Long) obj).sum();
            long useCount = pictureObjects.size();
            return SpaceUsageAnalyzeResponse.builder()
                    .usedSize(usedSize)
                    .maxSize(null)
                    .sizeUsageRatio(null)
                    .usedCount(useCount)
                    .maxCount(null)
                    .countUsageRatio(null)
                    .build();

        }
        // 指定空间,仅本人和管理员
        else {
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAM_ERROR);
            // 获取空间
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
            return SpaceUsageAnalyzeResponse.builder()
                    .usedSize(space.getTotalSize())
                    .maxSize(space.getMaxSize())
                    .sizeUsageRatio(NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue())
                    .usedCount(space.getTotalCount())
                    .maxCount(space.getMaxCount())
                    .countUsageRatio(NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue())
                    .build();


        }


    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
                                                                      User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        // 判断权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 查询包装器
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        //使用mybatis plus分类查询
        queryWrapper
                .select("category", "sum(picSize) as totalSize", "count(*) as Count")
                .groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = result.get("category") != null ? result.get("category").toString() : "未分类";

                    Number totalSizeNum = (Number) result.get("totalSize");
                    Number countNum = (Number) result.get("Count");

                    long totalSize = totalSizeNum != null ? totalSizeNum.longValue() : 0L;
                    long count = countNum != null ? countNum.longValue() : 0L;

                    return SpaceCategoryAnalyzeResponse.builder()
                            .category(category)
                            .totalSize(totalSize)
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());


    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        // 获取标签分析权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper
                .select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper()
                .selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .toList();
        Map<String, Long> tagMap = tagsJsonList
                .stream()
                .flatMap(tagJson -> JSONUtil.toList(tagJson, String.class)
                        .stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        return tagMap.entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        // 获取空间大小分析权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("picSize");
        List<Long> collect = pictureService.getBaseMapper()
                .selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .toList();
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", collect.stream().filter(size -> size < 1024 * 100).count());
        sizeRanges.put("100KB-500KB", collect.stream().filter(size -> size >= 1024 * 100 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", collect.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRanges.put(">1MB", collect.stream().filter(size -> size > 1024 * 1024).count());
        return sizeRanges.entrySet()
                .stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        // 获取空间用户分析权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);

        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        // 时间维度:每日，每周，每月
        // 时间维度:每日，每周，每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        if (TimeDimensionEnum.DAY.getValue().equals(timeDimension)) {
            queryWrapper.select("date_format(createTime, '%Y-%m-%d') as period", "count(*) as count")
                    .groupBy("date_format(createTime, '%Y-%m-%d')");
        } else if (TimeDimensionEnum.WEEK.getValue().equals(timeDimension)) {
            queryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count")
                    .groupBy("YEARWEEK(createTime)");
        } else if (TimeDimensionEnum.MONTH.getValue().equals(timeDimension)) {
            queryWrapper.select("date_format(createTime, '%Y-%m') as period", "count(*) as count")
                    .groupBy("date_format(createTime, '%Y-%m')");
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "时间维度错误");
        }

        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result ->
                        new SpaceUserAnalyzeResponse(
                                String.valueOf(result.get("period")),              // 安全转换，无论是 String 还是 Long
                                ((Number) result.get("count")).longValue()        // 用 Number 转换 Long
                        )
                ).collect(Collectors.toList());


    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTO_ERROR);
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());


        return spaceService.list(queryWrapper);
    }


    /**
     * 获取空间分析权限
     *
     * @param spaceAnalyzeRequest 请求
     * @param loginUser           登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        // 判断权限：全空间、公开空间，仅管理员
        if (queryAll || queryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTO_ERROR);
        } else {
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            // 判断权限：指定空间, 仅管理员和本人
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAM_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);

        }
    }


    /**
     * 填充查询条件
     *
     * @param spaceAnalyzeRequest 请求
     * @param queryWrapper        查询包装器
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {


        // 判断权限：全空间
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll) {
            return;
        }
        // 公共图库
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;

        }

        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未指定范围");
    }


}




