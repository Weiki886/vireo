package com.weiki.vireobackend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.weiki.vireobackend.model.entity.User;
import com.weiki.vireobackend.mapper.UserMapper;
import com.weiki.vireobackend.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Weiki886">weiki</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
