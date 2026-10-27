package com.weiki.vireobackend.constant;

/**
 * 用户相关常量。角色取值统一使用 {@link com.weiki.vireobackend.model.enums.UserRoleEnum}。
 */
public interface UserConstant {

    /**
     * 用户登录态在 Session 中的键
     */
    String USER_LOGIN_STATE = "user_login";

    /**
     * 管理员角色。注解属性必须使用编译期常量，因此这里保留字符串常量；
     * 运行时角色解析仍统一使用 UserRoleEnum。
     */
    String ADMIN_ROLE = "admin";

}
