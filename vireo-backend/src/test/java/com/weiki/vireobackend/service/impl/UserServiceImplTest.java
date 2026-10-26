package com.weiki.vireobackend.service.impl;

import com.weiki.vireobackend.exception.BusinessException;
import com.weiki.vireobackend.mapper.UserMapper;
import com.weiki.vireobackend.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {

    private AtomicInteger countByAccountCalls;

    private TestUserServiceImpl userService;

    @BeforeEach
    void setUp() {
        countByAccountCalls = new AtomicInteger();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userService = new TestUserServiceImpl(passwordEncoder);
        ReflectionTestUtils.setField(userService, "mapper", userMapper(countByAccountCalls));
    }

    @Test
    void userRegisterShouldSaveEncryptedUserAndReturnId() {
        long userId = userService.userRegister("weiki_01", "password123", "password123");

        assertThat(userId).isEqualTo(1893756824288542721L);
        User savedUser = userService.savedUser;
        assertThat(savedUser.getUserAccount()).isEqualTo("weiki_01");
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertThat(savedUser.getUserPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("password123", savedUser.getUserPassword())).isTrue();
        assertThat(savedUser.getUserPassword()).doesNotContain("password123");
        assertThat(savedUser.getUserName()).isEqualTo("用户");
        assertThat(savedUser.getUserRole()).isEqualTo("user");
    }

    @Test
    void userRegisterShouldRejectInvalidAccountFormat() {
        assertThatThrownBy(() -> userService.userRegister("weiki-01", "password123", "password123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户账号只能包含字母、数字和下划线");

        assertThat(countByAccountCalls).hasValue(0);
        assertThat(userService.savedUser).isNull();
    }

    @Test
    void userRegisterShouldConvertDuplicateKeyException() {
        userService.saveException = new DuplicateKeyException("duplicate userAccount");

        assertThatThrownBy(() -> userService.userRegister("weiki_01", "password123", "password123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号重复");
    }

    @SuppressWarnings("unchecked")
    private static UserMapper userMapper(AtomicInteger countByAccountCalls) {
        return (UserMapper) Proxy.newProxyInstance(
                UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class},
                (proxy, method, args) -> {
                    if ("selectCountByQuery".equals(method.getName())) {
                        countByAccountCalls.incrementAndGet();
                        return 0L;
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
