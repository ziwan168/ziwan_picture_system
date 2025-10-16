package com.ziwan.ziwanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziwan.ziwanpicturebackend.model.dto.space.SpaceAddRequest;
import com.ziwan.ziwanpicturebackend.model.dto.space.SpaceQueryRequest;
import com.ziwan.ziwanpicturebackend.model.entity.Space;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.model.vo.SpaceVO;


import javax.servlet.http.HttpServletRequest;

/**
 * @author brave
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-10-15 18:40:52
 */
public interface SpaceService extends IService<Space> {


    /**
     * 校验
     *
     * @param space
     * @param add   是否为创建时校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类（单条）
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);


    /**
     * 获取空间包装类（分页）
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 填充空间等级
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 添加空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

}
