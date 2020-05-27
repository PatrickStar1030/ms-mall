package com.dilemma.order.interceptor;

import com.dilemma.auth.entity.UserInfo;
import com.dilemma.auth.jwt.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal();

    private JwtOperator operator;

    public LoginInterceptor(JwtOperator operator) {
        this.operator = operator;
    }

    /**
     * 拦截器校验登陆用户是否合法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)){
            //未登录返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        boolean boo = operator.validateToken(token);
        if (boo){
            //解析用户信息
            try {
                Claims claims = operator.getClaimsFromToken(token);
                UserInfo userInfo = new UserInfo(Long.valueOf(claims.get("userId").toString()),(String) claims.get("username"));
                tl.set(userInfo);
                return true;
            } catch (NumberFormatException e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                log.error("token解析失败：{}",e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户信息
        tl.remove();
    }

    public static UserInfo getLoginUser(){
        return tl.get();
    }
}
