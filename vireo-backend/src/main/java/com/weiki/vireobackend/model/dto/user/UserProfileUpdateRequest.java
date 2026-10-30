package com.weiki.vireobackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户可自行修改的资料。
 *
 * <p>刻意不包含 id、账号、密码和角色字段，用户身份只从 Session 获取。</p>
 */
@Data
public class UserProfileUpdateRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
