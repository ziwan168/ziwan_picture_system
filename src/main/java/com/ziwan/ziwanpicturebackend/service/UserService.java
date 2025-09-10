package com.ziwan.ziwanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ziwan.ziwanpicturebackend.model.dto.user.UserQueryRequest;
import com.ziwan.ziwanpicturebackend.model.dto.user.UserRegisterRequest;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziwan.ziwanpicturebackend.model.vo.UserLoginVO;
import com.ziwan.ziwanpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author brave
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-09-02 18:42:36
 */
public interface UserService extends IService<User> {
    long userRegister(UserRegisterRequest userRegisterRequest);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      httpRequest 请求方便设置 cookie
     * @return 脱敏后的用户信息
     */
    UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 生成加密密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);


    /**
     * 获取脱敏的登录用户信息
     *
     * @return
     */
    UserLoginVO getLoginUserVO(User user);


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    UserVO getUserVO(User user);


    /**
     * 获取脱敏的用户列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
