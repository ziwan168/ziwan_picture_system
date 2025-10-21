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
 * &#064;description  针对表【space(空间)】的数据库操作Service
 * &#064;createDate  2025-10-15 18:40:52
 */
public interface SpaceService extends IService<Space> {


    /**
     * 校验
     *
     * @param space 空间
     * @param add   是否为创建时校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类（单条）
     *
     * @param space 空间
     * @param request 请求
     * @return 空间包装类
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);


    /**
     * 获取空间包装类（分页）
     *
     * @param spacePage 空间分页
     * @param request   请求
     * @return 空间包装类分页
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest 空间查询条件
     * @return 空间查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 填充空间等级
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加条件
     * @param loginUser 登录用户
     * @return 添加的空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 校验空间权限
     *
     * @param loginUser 登录用户
     * @param space     空间
     */
    void checkSpaceAuth(User loginUser, Space space);


}
