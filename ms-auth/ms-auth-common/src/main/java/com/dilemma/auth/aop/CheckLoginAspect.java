package com.dilemma.auth.aop;


import com.dilemma.auth.jwt.JwtOperator;
import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class CheckLoginAspect {
    @Autowired
    private JwtOperator operator;

    @Around("@annotation(com.dilemma.auth.aop.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point) throws Throwable {
        //1、获取http中的header
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        //2、校验token是否合法，如果合法就认为用户已经登陆，不合法返回401
        if (StringUtils.isBlank(token)){
            throw new MsException(ExceptionEnum.AUTHORIZATION_FAIL);
        }
        boolean isValid = operator.validateToken(token);
        if (!isValid){
            throw new MsException(ExceptionEnum.AUTHORIZATION_FAIL);
        }
        return point.proceed();
    }
}
