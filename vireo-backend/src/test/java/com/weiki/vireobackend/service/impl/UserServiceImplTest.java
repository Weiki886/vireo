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
import java.util.concurrent.atomic.AtomicLong;

import static com.weiki.vireobackend.constant.UserConstant.USER_LOGIN_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {

    private static final String ACCOUNT = "weiki_01";
    private static final String RAW_PASSWORD = "password123";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AtomicInteger countByAccountCalls;

    private AtomicLong existingCount;

    private TestUserServiceImpl userService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        countByAccountCalls = new AtomicInteger();
        existingCount = new AtomicLong();
        userService = new TestUserServiceImpl(passwordEncoder);
        request = new MockHttpServletRequest();
    }

    // ---------- 注册 ----------

    @Test
    void userRegisterShouldSaveEncryptedUserAndReturnId() {
        useMapper(persistedUser(), persistedUser());
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
        useMapper(persistedUser(), persistedUser());
        assertThatThrownBy(() -> userService.userRegister("weiki-01", RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户账号只能包含字母、数字和下划线");

        assertThat(countByAccountCalls).hasValue(0);
        assertThat(userService.savedUser).isNull();
    }

    @Test
    void userRegisterShouldRejectBlankParams() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userRegister("  ", RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("参数为空");
        assertThat(countByAccountCalls).hasValue(0);
    }

    @Test
    void userRegisterShouldRejectShortPassword() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userRegister(ACCOUNT, "short", "short"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户密码过短");
        assertThat(countByAccountCalls).hasValue(0);
    }

    @Test
    void userRegisterShouldRejectMismatchedPassword() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userRegister(ACCOUNT, RAW_PASSWORD, "otherpass1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("两次输入的密码不一致");
        assertThat(countByAccountCalls).hasValue(0);
    }

    @Test
    void userRegisterShouldRejectWhenAccountAlreadyExists() {
        useMapper(persistedUser(), persistedUser());
        existingCount.set(1);

        assertThatThrownBy(() -> userService.userRegister(ACCOUNT, RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号重复");
        assertThat(userService.savedUser).isNull();
    }

    @Test
    void userRegisterShouldThrowSystemErrorWhenSaveFails() {
        useMapper(persistedUser(), persistedUser());
        userService.saveReturnsFalse = true;

        assertThatThrownBy(() -> userService.userRegister(ACCOUNT, RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("注册失败，数据库错误")
                .extracting("code").isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
    }

    @Test
    void userRegisterShouldConvertDuplicateKeyException() {
        useMapper(persistedUser(), persistedUser());
        userService.saveException = new DuplicateKeyException("duplicate userAccount");

        assertThatThrownBy(() -> userService.userRegister(ACCOUNT, RAW_PASSWORD, RAW_PASSWORD))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号重复");
    }

    // ---------- 登录 ----------

    @Test
    void userLoginShouldMatchBcryptAndStoreDesensitizedVoInSession() {
        useMapper(persistedUser(), persistedUser());

        LoginUserVO loginUserVO = userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        assertThat(loginUserVO.getId()).isEqualTo(persistedUser().getId());
        assertThat(loginUserVO.getUserAccount()).isEqualTo(ACCOUNT);
        // Session 中存的是脱敏 VO，而不是带密码哈希的实体
        Object sessionUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        assertThat(sessionUser).isInstanceOf(LoginUserVO.class);
        assertThat(sessionUser).isNotInstanceOf(User.class);
        assertThat(sessionUser).usingRecursiveComparison().isEqualTo(loginUserVO);
    }

    @Test
    void userLoginShouldRejectWrongPassword() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userLogin(ACCOUNT, "wrongpass9", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在或密码错误");
        assertThat(request.getSession().getAttribute(USER_LOGIN_STATE)).isNull();
    }

    @Test
    void userLoginShouldRejectUnknownAccount() {
        useMapper(null, null);

        assertThatThrownBy(() -> userService.userLogin(ACCOUNT, RAW_PASSWORD, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在或密码错误");
    }

    @Test
    void userLoginShouldRejectBlankParams() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userLogin(" ", RAW_PASSWORD, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("参数为空");
    }

    @Test
    void userLoginShouldRejectInvalidAccountFormat() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userLogin("abc", RAW_PASSWORD, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号错误");
    }

    @Test
    void userLoginShouldRejectShortPassword() {
        useMapper(persistedUser(), persistedUser());

        assertThatThrownBy(() -> userService.userLogin(ACCOUNT, "short", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("密码错误");
    }

    @Test
    void getLoginUserVoShouldReturnNullWhenUserIsNull() {
        assertThat(userService.getLoginUserVO(null)).isNull();
    }

    @Test
    void getLoginUserVoShouldCopyFieldsWithoutPassword() {
        User user = persistedUser();

        LoginUserVO vo = userService.getLoginUserVO(user);

        assertThat(vo.getId()).isEqualTo(user.getId());
        assertThat(vo.getUserAccount()).isEqualTo(user.getUserAccount());
        assertThat(vo.getUserName()).isEqualTo(user.getUserName());
        assertThat(vo.getUserRole()).isEqualTo(user.getUserRole());
    }

    // ---------- 当前登录用户 / 退出 ----------

    @Test
    void getLoginUserShouldReloadFromDatabaseAndReturnEntityWhenLoggedIn() {
        useMapper(persistedUser(), persistedUser());
        userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        User loginUser = userService.getLoginUser(request);
        // Service 返回完整实体（供 AOP 读角色），脱敏由 Controller 负责
        assertThat(loginUser.getId()).isEqualTo(persistedUser().getId());
        assertThat(loginUser.getUserAccount()).isEqualTo(ACCOUNT);
    }

    @Test
    void getLoginUserShouldThrowNotLoginWhenSessionEmpty() {
        assertThatThrownBy(() -> userService.getLoginUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录")
                .extracting("code").isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getCode());
    }

    @Test
    void getLoginUserShouldThrowNotLoginWhenUserDeleted() {
        // 登录时能查到用户，但之后用户被删除（按 id 查不到），应视为未登录
        useMapper(persistedUser(), null);
        userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        assertThatThrownBy(() -> userService.getLoginUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录")
                .extracting("code").isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getCode());
    }

    @Test
    void userLogoutShouldClearSession() {
        useMapper(persistedUser(), persistedUser());
        userService.userLogin(ACCOUNT, RAW_PASSWORD, request);

        assertThat(userService.userLogout(request)).isTrue();
        assertThat(request.getSession().getAttribute(USER_LOGIN_STATE)).isNull();
        assertThatThrownBy(() -> userService.getLoginUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录");
    }

    @Test
    void userLogoutShouldThrowNotLoginWhenNotLoggedIn() {
        assertThatThrownBy(() -> userService.userLogout(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录")
                .extracting("code").isEqualTo(ErrorCode.NOT_LOGIN_ERROR.getCode());
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

    /**
     * @param queryUser 按账号查询（selectOneByQuery）返回的用户
     * @param byIdUser  按 id 查询（selectOneById）返回的用户，null 模拟已被删除
     */
    private void useMapper(User queryUser, User byIdUser) {
        ReflectionTestUtils.setField(userService, "mapper",
                userMapper(countByAccountCalls, existingCount, queryUser, byIdUser));
    }

    @SuppressWarnings("unchecked")
    private static UserMapper userMapper(AtomicInteger countByAccountCalls, AtomicLong existingCount,
                                         User queryUser, User byIdUser) {
        return (UserMapper) Proxy.newProxyInstance(
                UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "selectCountByQuery":
                            countByAccountCalls.incrementAndGet();
                            return existingCount.get();
                        case "selectOneByQuery":
                            return queryUser;
                        case "selectOneById":
                            return byIdUser;
                        default:
                            break;
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

        private boolean saveReturnsFalse;

        TestUserServiceImpl(PasswordEncoder passwordEncoder) {
            super(passwordEncoder);
        }

        @Override
        public boolean save(User user) {
            if (saveException != null) {
                throw saveException;
            }
            if (saveReturnsFalse) {
                return false;
            }
            savedUser = user;
            user.setId(1893756824288542721L);
            return true;
        }
    }
}
