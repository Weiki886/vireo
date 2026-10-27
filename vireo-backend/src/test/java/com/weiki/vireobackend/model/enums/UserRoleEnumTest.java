package com.weiki.vireobackend.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleEnumTest {

    @Test
    void getEnumByValueShouldResolveKnownRoles() {
        assertThat(UserRoleEnum.getEnumByValue("user")).isEqualTo(UserRoleEnum.USER);
        assertThat(UserRoleEnum.getEnumByValue("admin")).isEqualTo(UserRoleEnum.ADMIN);
    }

    @Test
    void getEnumByValueShouldReturnNullForUnknownValue() {
        assertThat(UserRoleEnum.getEnumByValue("root")).isNull();
    }

    @Test
    void getEnumByValueShouldReturnNullForBlankOrNull() {
        assertThat(UserRoleEnum.getEnumByValue("")).isNull();
        assertThat(UserRoleEnum.getEnumByValue(null)).isNull();
    }

    @Test
    void enumShouldExposeExpectedValuesForAuthChecks() {
        // AOP 权限切面依赖这两个取值，锁定契约防止被误改
        assertThat(UserRoleEnum.USER.getValue()).isEqualTo("user");
        assertThat(UserRoleEnum.ADMIN.getValue()).isEqualTo("admin");
    }
}
