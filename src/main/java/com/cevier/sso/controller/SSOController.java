package com.cevier.sso.controller;

import com.alibaba.fastjson.JSONObject;
import com.cevier.sso.service.UserService;
import com.cevier.sso.utils.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/login")
    public String login(@RequestParam String returnUrl,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        var token = getCookie(request, "loginInfo");

        // 验证token
        if (StringUtils.hasText(token)) {
            String userId = redisUtils.get("token:" + token);
            if (StringUtils.hasText(userId)) {
                var userInfo = redisUtils.get("userInfo:" + userId);
                if (StringUtils.hasText(userInfo)) {
                    // 生成临时登入凭证
                    var ticket = UUID.randomUUID().toString();
                    redisUtils.set("ticket:" + ticket, token, 600);
                    return "redirect:" + returnUrl + "?ticket=" + ticket;
                }
            }
        }

        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }


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


    /**
     * 验证ticket
     */
    @GetMapping("/verify-ticket")
    @ResponseBody
    public boolean verifyTicket(@RequestParam String ticket, HttpServletRequest request, HttpServletResponse response) {

        if (!StringUtils.hasText(ticket)) {
            return false;
        }

        var token = redisUtils.get("ticket:" + ticket);
        if (!StringUtils.hasText(token)) {
            return false;
        }

        var userId = redisUtils.get("token:" + token);
        if (!StringUtils.hasText(userId)) {
            return false;
        }

        var userInfo = redisUtils.get("userInfo:" + userId);
        if (!StringUtils.hasText(userInfo)) {
            return false;
        }

        setCookie("userInfo", userInfo, response);
        return true;
    }


    /**
     * 验证ticket
     */
    @GetMapping("/logout")
    @ResponseBody
    public String logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response) {
        // 删除token
        var token = getCookie(request, "loginInfo");
        redisUtils.del("token:" + token);
        // 清除cookie
        deleteCookie("loginInfo", response);
        // 清除全局会话
        redisUtils.del("userInfo:" + userId);
        return "bye";
    }


    private void setCookie(String key,
                           String val,
                           HttpServletResponse response) {
        Cookie cookie = new Cookie(key, val);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    private String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || StringUtils.hasText(key)) {
            return null;
        }

        String cookieValue = null;
        for (Cookie cookie : cookieList) {
            if (cookie.getName().equals(key)) {
                cookieValue = cookie.getValue();
                break;
            }
        }

        return cookieValue;
    }

    private void deleteCookie(String key,
                              HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 3600); // 7d
        response.addCookie(cookie);
    }
}
