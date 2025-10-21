package com.ziwan.ziwanpicturebackend.service;


import com.ziwan.ziwanpicturebackend.model.dto.space.analyze.*;
import com.ziwan.ziwanpicturebackend.model.entity.Space;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.vo.space.*;

import java.util.List;

/**
 * @author brave
 * &#064;createDate  2025-10-15 18:40:52
 */
public interface SpaceAnalyzeService {

    /**
     * 获取空间使用分析
     * @param spaceUsageAnalyzeRequest 请求
     * @param loginUser 登录用户
     * @return 空间使用分析
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片分类分析
     * @param spaceCategoryAnalyzeRequest 请求
     * @param loginUser 登录用户
     * @return 空间分类分析
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);


    /**
     * 获取空间标签分析
     *
     * @param spaceTagAnalyzeRequest  SpaceTagAnalyzeRequests
     * @param loginUser 登录用户
     * @return  List<SpaceTagAnalyzeResponse>
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest  SpaceSizeAnalyzeRequests
     * @param loginUser 登录用户
     * @return  List<SpaceSizeAnalyzeResponse>
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);


    /**
     * 用户上传时间分析
     *
     * @param spaceUserAnalyzeRequest  spaceUserAnalyzeRequest
     * @param loginUser 当前登录的用户
     * @return List<SpaceUserAnalyzeResponse>
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);



    /**
     * 空间使用排行 仅管理员可用
     *
     * @param spaceRankAnalyzeRequest spaceRankAnalyzeRequest
     * @param loginUser 当前登录的用户
     * @return List<Space>
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);






}
