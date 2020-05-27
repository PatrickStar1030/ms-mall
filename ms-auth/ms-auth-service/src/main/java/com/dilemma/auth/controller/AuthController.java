package com.dilemma.auth.controller;

import com.dilemma.auth.aop.CheckLogin;
import com.dilemma.auth.config.JwtProperties;
import com.dilemma.auth.entity.UserInfo;
import com.dilemma.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
@RequestMapping("auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登陆授权
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Map<String,String>> authentication(@RequestParam("username") String username,
                                                             @RequestParam("password") String password) {
        Map<String,String> tokenMap = new HashMap<>();
        //登陆校验
        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        tokenMap.put("token",token);
        //log.info("token:{}",tokenMap.get("token"));
        //使用cookie携带token方式验证，获取真实域名
        //String realServerName = request.getHeader("realServerName");
        //将token写入cookie，并指定httpOnly为true，防止通过js获取和修改
        //CookieUtils.setCookie(request, response, properties.getCookieName(), token, realServerName, properties.getCookieMaxAge(), null, true);
        return ResponseEntity.ok(tokenMap);
    }

    @GetMapping("verify")
    @CheckLogin
    public ResponseEntity<UserInfo> verifyUser(@RequestHeader("token") String token){
        try {
            //从token中解析token
            UserInfo userInfo = this.authService.verifyUser(token);
            if (userInfo != null)
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            log.info("解析用户信息失败：{}",e.getMessage());
            //出现异常响应401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
