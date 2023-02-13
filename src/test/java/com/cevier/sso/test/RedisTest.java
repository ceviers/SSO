package com.cevier.sso.test;

import com.cevier.sso.pojo.User;
import com.cevier.sso.service.UserService;
import com.cevier.sso.utils.RedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author: cevier.wei
 * @date: 2023/2/13 15:14
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserService userService;

    @Test
    public void test() {
        redisUtils.set("kkk", "222");
        System.out.println(redisUtils.get("kkk"));
    }

    @Test
    public void test2() {
        System.out.println(userService.lambdaQuery().eq(User::getId, 1L).one());
    }
}
