package com.weiki.vireobackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.mapper.UserMapper;
import com.weiki.vireobackend.model.enums.UserRoleEnum;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import com.weiki.vireobackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import static com.weiki.vireobackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Weiki886">weiki</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 账号长度 4-16 位，只能包含字母、数字和下划线。
     */
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,16}$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (!ACCOUNT_PATTERN.matcher(userAccount).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号只能包含字母、数字和下划线，长度为 4-16 位");
        }
        if (userPassword.length() < MIN_PASSWORD_LENGTH || checkPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复。唯一索引是最终保障，并发下的 DuplicateKeyException 也会转换为业务异常。
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密。BCrypt 每次自动生成随机盐，同一密码多次加密结果也不同。
        String encryptPassword = encryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult;
        try {
            saveResult = this.save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (!ACCOUNT_PATTERN.matcher(userAccount).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 仅按账号查询用户。BCrypt 每次哈希的盐不同，无法用“重新加密后等值匹配”来验密。
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userAccount", userAccount);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 3. 使用 matches 比对明文与库中哈希；账号不存在和密码错误统一提示，避免账号枚举。
        if (user == null || !passwordEncoder.matches(userPassword, user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4. 记录登录态。Session 中只存脱敏后的 VO，避免密码哈希进入会话。
        LoginUserVO loginUserVO = getLoginUserVO(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(userObj instanceof LoginUserVO loginUserVO)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        return loginUserVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 使用 BCrypt 加密用户密码，避免明文密码入库。
     *
     * @param rawPassword 原始密码
     * @return BCrypt 哈希
     */
    private String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
