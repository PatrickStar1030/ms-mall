package com.dilemma.cart.interceptor;

import com.dilemma.auth.entity.UserInfo;
import com.dilemma.auth.jwt.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登陆拦截器
 */
@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
    //threadLocal存放登陆用户
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    private JwtOperator operator;

    public LoginInterceptor(JwtOperator operator){
        this.operator = operator;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //1、查询token是否存在
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)){
            //未登录返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        try {
            //存在token，解析token信息
            Claims claims = this.operator.getClaimsFromToken(token);
            UserInfo userInfo = new UserInfo(Long.valueOf(claims.get("userId").toString()),(String) claims.get("username"));
            tl.set(userInfo);
            return true;
        } catch (Exception e) {
            //抛出异常，证明未登录，返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            log.error("token解析失败：{}",e.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getLoginUser(){
        return tl.get();
    }
}
