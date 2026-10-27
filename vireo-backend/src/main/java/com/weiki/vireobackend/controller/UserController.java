package com.weiki.vireobackend.controller;

import com.mybatisflex.core.paginate.Page;
import com.weiki.vireobackend.common.BaseResponse;
import com.weiki.vireobackend.common.DeleteRequest;
import com.weiki.vireobackend.common.ResultUtils;
import com.weiki.vireobackend.annotation.AuthCheck;
import com.weiki.vireobackend.constant.UserConstant;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.exception.ThrowUtils;
import com.weiki.vireobackend.model.dto.user.UserAddRequest;
import com.weiki.vireobackend.model.dto.user.UserLoginRequest;
import com.weiki.vireobackend.model.dto.user.UserQueryRequest;
import com.weiki.vireobackend.model.dto.user.UserRegisterRequest;
import com.weiki.vireobackend.model.dto.user.UserUpdateRequest;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import com.weiki.vireobackend.model.vo.UserVO;
import com.weiki.vireobackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关接口：注册、登录、获取当前登录用户、退出登录以及管理员用户管理。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final int MAX_PAGE_SIZE = 100;

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求
     * @return 脱敏后的登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户（用于前端刷新页面后恢复登录态）。
     *
     * @param request 请求（从 Session 读取登录态）
     * @return 脱敏后的登录用户信息，未登录时返回 40100
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 退出登录，清除 Session 中的登录态。
     *
     * @param request 请求
     * @return 是否退出成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 管理员创建用户，新用户使用默认密码。
     *
     * @param userAddRequest 创建用户请求
     * @return 新用户 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody(required = false) UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.addUser(userAddRequest));
    }

    /**
     * 根据 id 获取脱敏用户信息（仅管理员）。
     *
     * @param id 用户 id
     * @return 脱敏用户信息
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserVOById(@RequestParam long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户（仅管理员）。
     *
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody(required = false) DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null
                || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.removeById(deleteRequest.getId()));
    }

    /**
     * 更新用户（仅管理员）。
     *
     * @param userUpdateRequest 更新用户请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody(required = false) UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.updateUser(userUpdateRequest));
    }

    /**
     * 分页获取脱敏用户列表（仅管理员）。
     *
     * @param userQueryRequest 查询请求参数
     * @return 用户分页数据
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(
            @RequestBody(required = false) UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0 || pageSize > MAX_PAGE_SIZE,
                ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = userPage.map(userService::getUserVO);
        return ResultUtils.success(userVOPage);
    }

}
