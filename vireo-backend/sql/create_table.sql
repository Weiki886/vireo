# 数据库初始化
# @author <a href="https://github.com/Weiki886">weiki</a>

-- 创建库
create database if not exists vireo default character set utf8mb4 collate utf8mb4_unicode_ci;

-- 切换库
use vireo;

-- 用户表
create table if not exists user
(
    id            bigint comment 'id（应用层雪花算法生成）' primary key,
    userAccount   varchar(256)                           not null comment '账号',
    userPassword  varchar(512)                           not null comment '密码',
    userName      varchar(256)                           null comment '用户昵称',
    userAvatar    varchar(1024)                          null comment '用户头像',
    userProfile   varchar(512)                           null comment '用户简介',
    userRole      varchar(32) default 'user'             not null comment '用户角色：user/admin',
    editTime      datetime      default CURRENT_TIMESTAMP not null comment '编辑时间（应用层维护，暂未使用）',
    createTime    datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint       default 0                 not null comment '是否删除',
    vipExpireTime datetime                                 null comment '会员过期时间',
    vipCode       varchar(128)                           null comment '会员兑换码',
    vipNumber     bigint                                 null comment '会员编号',
    shareCode     varchar(20)                            null comment '分享码',
    inviteUser    bigint                                 null comment '邀请用户 id',
    -- 账号释放策略：注销时应用层需先将 userAccount 改写为「原账号#deleted#id」再标记 isDelete=1，从而释放账号名供再次注册
    UNIQUE KEY uk_userAccount (userAccount),
    UNIQUE KEY uk_vipNumber (vipNumber),
    UNIQUE KEY uk_shareCode (shareCode),
    INDEX idx_userName (userName),
    INDEX idx_inviteUser (inviteUser)
) comment '用户' collate = utf8mb4_unicode_ci;
