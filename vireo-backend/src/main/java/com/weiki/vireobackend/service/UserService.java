package com.weiki.vireobackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.weiki.vireobackend.model.dto.user.UserAddRequest;
import com.weiki.vireobackend.model.dto.user.UserProfileUpdateRequest;
import com.weiki.vireobackend.model.dto.user.UserUpdateRequest;
import com.weiki.vireobackend.model.dto.user.UserQueryRequest;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import com.weiki.vireobackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

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
     * 获取当前登录用户。从 Session 读取登录态后再查库，返回最新的完整用户实体
     * （供权限切面读取角色）；出参脱敏由 Controller 层负责。
     *
     * @param request 请求
     * @return 当前登录用户实体，未登录时抛出 BusinessException(40100)
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录，清除 Session 中的登录态。
     *
     * @param request 请求
     * @return 是否退出成功，未登录时抛出 BusinessException(40100)
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户实体
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 管理员创建用户，新用户使用默认密码。
     *
     * @param userAddRequest 创建用户请求
     * @return 新用户 id
     */
    long addUser(UserAddRequest userAddRequest);

    /**
     * 管理员更新用户信息。
     *
     * @param userUpdateRequest 更新用户请求
     * @return 是否更新成功
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 更新当前登录用户可自行维护的资料。
     *
     * @param loginUser            从 Session 解析出的当前用户
     * @param userProfileUpdateRequest 当前用户资料更新请求
     * @return 更新后的脱敏登录用户信息
     */
    LoginUserVO updateCurrentUser(User loginUser, UserProfileUpdateRequest userProfileUpdateRequest);

    /**
     * 获取脱敏的用户视图对象。
     *
     * @param user 用户实体
     * @return 脱敏用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取脱敏的用户视图对象。
     *
     * @param userList 用户实体列表
     * @return 脱敏用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据管理端用户查询请求构造 MyBatis-Flex 查询条件。
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件构造器
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

}
