package com.cevier.sso.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cevier.sso.mapper.UserMapper;
import com.cevier.sso.pojo.User;
import org.springframework.stereotype.Service;

/**
 * @author: cevier.wei
 * @date: 2023/2/13 15:21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User varyUserLoginInfo(String username, String password) {
        // 这里需要校验用户名和密码是否匹配
        return this.lambdaQuery().eq(User::getName, username).last("LIMIT 1").one();
    }
}
