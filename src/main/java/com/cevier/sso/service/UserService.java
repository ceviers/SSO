package com.cevier.sso.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cevier.sso.pojo.User;

/**
 * @author: cevier.wei
 * @date: 2023/2/13 15:21
 */
public interface UserService extends IService<User> {
    User varyUserLoginInfo(String username, String password);
}
