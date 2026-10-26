package com.weiki.vireobackend.service;

import com.mybatisflex.core.service.IService;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/Weiki886">weiki</a>
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
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求（用于写入 Session 登录态）
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户（从 Session 读取）
     *
     * @param request 请求
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request 请求（用于清除 Session 登录态）
     * @return 是否退出成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户实体
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

}
