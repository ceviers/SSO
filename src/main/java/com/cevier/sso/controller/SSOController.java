package com.cevier.sso.controller;

import com.alibaba.fastjson.JSONObject;
import com.cevier.sso.service.UserService;
import com.cevier.sso.utils.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author: cevier.wei
 * @date: 2023/2/13 15:30
 */
@Controller
public class SSOController {
    @Resource
    private UserService userService;

    @Resource
    private RedisUtils redisUtils;

    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String returnUrl,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {

        // 校验用户名和密码
        var user = userService.varyUserLoginInfo(username, password);
        if (user == null) {
            model.addAttribute("errMsg", "用户名或密码不正确");
            model.addAttribute("returnUrl", returnUrl);
            return "login";
        }

        // 将用户会话存入缓存
        redisUtils.set("userInfo:" + user.getId(), JSONObject.toJSONString(user));

        // 生成token，并将其存入缓存和cookie
        var token = UUID.randomUUID().toString();
        setCookie("loginInfo", token, response);
        redisUtils.set("token:" + token, user.getId().toString());

        // 颁发临时登入凭证
        var ticket = UUID.randomUUID().toString();
        redisUtils.set("ticket:" + ticket, token, 600);

        return "redirect:" + returnUrl + "?ticket=" + ticket;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }

    private void setCookie(String key,
                           String val,
                           HttpServletResponse response) {

        Cookie cookie = new Cookie(key, val);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
