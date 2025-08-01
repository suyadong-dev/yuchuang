package com.yadong.yuchuang.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yadong.yuchuang.model.dto.user.UserQueryRequest;
import com.yadong.yuchuang.model.entity.User;
import com.yadong.yuchuang.model.vo.LoginUserVO;
import com.yadong.yuchuang.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用户表 服务层。
 *
 * @author 超人不会飞
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 已登录用户
     * @return 脱敏后的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的user信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的user列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取user封装的查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
