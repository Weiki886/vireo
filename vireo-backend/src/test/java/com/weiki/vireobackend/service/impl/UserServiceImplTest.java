package com.weiki.vireobackend.service.impl;

import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.exception.ErrorCode;
import com.weiki.vireobackend.mapper.UserMapper;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.model.vo.LoginUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static com.weiki.vireobackend.constant.UserConstant.USER_LOGIN_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {

    private static final String ACCOUNT = "weiki_01";
    private static final String RAW_PASSWORD = "password123";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AtomicInteger countByAccountCalls;

    private TestUserServiceImpl userService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        countByAccountCalls = new AtomicInteger();
        userService = new TestUserServiceImpl(passwordEncoder);
        request = new MockHttpServletRequest();
    }

    // ---------- 注册 ----------

    @Test
    void userRegisterShouldSaveEncryptedUserAndReturnId() {
        useMapper(null);
        long userId = userService.userRegister(ACCOUNT, RAW_PASSWORD, RAW_PASSWORD);

        assertThat(userId).isEqualTo(1893756824288542721L);
        User savedUser = userService.savedUser;
        assertThat(savedUser.getUserAccount()).isEqualTo(ACCOUNT);
        assertThat(savedUser.getUserPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(RAW_PASSWORD, savedUser.getUserPassword())).isTrue();
        assertThat(savedUser.getUserPassword()).doesNotContain(RAW_PASSWORD);
        assertThat(savedUser.getUserName()).isEqualTo("用户");
        assertThat(savedUser.getUserRole()).isEqualTo("user");
    }

    @Test
    void userRegisterShouldRejectInvalidAccountFormat() {
        useMapper(null);
        assertThatThrownBy(() -> userService.userRegister("weiki-01", RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户账号只能包含字母、数字和下划线");

        assertThat(countByAccountCalls).hasValue(0);
        assertThat(userService.savedUser).isNull();
    }

    @Test
    void userRegisterShouldConvertDuplicateKeyException() {
        useMapper(null);
        userService.saveException = new DuplicateKeyException("duplicate userAccount");

        assertThatThrownBy(() -> userService.userRegister(ACCOUNT, RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号重复");
    }

    // ---------- 登录 ----------

    @Test
    void userLoginShouldMatchBcryptAndStoreDesensitizedVoInSession() {
        User existingUser = persistedUser();
        useMapper(existingUser);

        LoginUserVO loginUserVO = userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        assertThat(loginUserVO.getId()).isEqualTo(existingUser.getId());
        assertThat(loginUserVO.getUserAccount()).isEqualTo(ACCOUNT);
        // Session 中存的是脱敏 VO，而不是带密码哈希的实体
        Object sessionUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        assertThat(sessionUser).isInstanceOf(LoginUserVO.class);
        assertThat(sessionUser).isNotInstanceOf(User.class);
        assertThat(sessionUser).usingRecursiveComparison().isEqualTo(loginUserVO);
    }

    @Test
    void userLoginShouldRejectWrongPassword() {
        useMapper(persistedUser());

        assertThatThrownBy(() -> userService.userLogin(ACCOUNT, "wrongpass9", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在或密码错误");
        assertThat(request.getSession().getAttribute(USER_LOGIN_STATE)).isNull();
    }

    @Test
    void userLoginShouldRejectUnknownAccount() {
        useMapper(null);

        assertThatThrownBy(() -> userService.userLogin(ACCOUNT, RAW_PASSWORD, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在或密码错误");
    }

    @Test
    void userLoginShouldRejectBlankParams() {
        useMapper(persistedUser());

        assertThatThrownBy(() -> userService.userLogin(" ", RAW_PASSWORD, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("参数为空");
    }

    // ---------- 当前登录用户 / 退出 ----------

    @Test
    void getLoginUserShouldReturnSessionUserWhenLoggedIn() {
        useMapper(persistedUser());
        userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        assertThat(userService.getLoginUser(request).getUserAccount()).isEqualTo(ACCOUNT);
    }

    @Test
    void getLoginUserShouldThrowNotLoginWhenSessionEmpty() {
        assertThatThrownBy(() -> userService.getLoginUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录")
                .extracting("code").isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getCode());
    }

    @Test
    void userLogoutShouldClearSession() {
        useMapper(persistedUser());
        userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        assertThat(userService.userLogout(request)).isTrue();
        assertThat(request.getSession().getAttribute(USER_LOGIN_STATE)).isNull();
        assertThatThrownBy(() -> userService.getLoginUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录");
    }

    @Test
    void userLogoutShouldThrowWhenNotLoggedIn() {
        assertThatThrownBy(() -> userService.userLogout(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录");
    }

    // ---------- 辅助 ----------

    private User persistedUser() {
        return User.builder()
                .id(1893756824288542721L)
                .userAccount(ACCOUNT)
                .userPassword(passwordEncoder.encode(RAW_PASSWORD))
                .userName("用户")
                .userRole("user")
                .build();
    }

    private void useMapper(User loginUser) {
        ReflectionTestUtils.setField(userService, "mapper",
                userMapper(countByAccountCalls, loginUser));
    }

    @SuppressWarnings("unchecked")
    private static UserMapper userMapper(AtomicInteger countByAccountCalls, User loginUser) {
        return (UserMapper) Proxy.newProxyInstance(
                UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> {
                    if ("selectCountByQuery".equals(method.getName())) {
                        countByAccountCalls.incrementAndGet();
                        return 0L;
                    }
                    if ("selectOneByQuery".equals(method.getName())) {
                        return loginUser;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class)) {
                        return false;
                    }
                    if (returnType.equals(long.class)) {
                        return 0L;
                    }
                    if (returnType.equals(int.class)) {
                        return 0;
                    }
                    return null;
                });
    }

    private static class TestUserServiceImpl extends UserServiceImpl {

        private User savedUser;

        private RuntimeException saveException;

        TestUserServiceImpl(PasswordEncoder passwordEncoder) {
            super(passwordEncoder);
        }

        @Override
        public boolean save(User user) {
            if (saveException != null) {
                throw saveException;
            }
            savedUser = user;
            user.setId(1893756824288542721L);
            return true;
        }
    }
}
