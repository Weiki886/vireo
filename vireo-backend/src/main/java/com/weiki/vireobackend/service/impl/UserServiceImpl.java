package com.weiki.vireobackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.mapper.UserMapper;
import com.weiki.vireobackend.model.enums.UserRoleEnum;
import com.weiki.vireobackend.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

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
