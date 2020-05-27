package com.dilemma.cart.config;

import com.dilemma.auth.config.JwtProperties;
import com.dilemma.auth.jwt.JwtOperator;
import com.dilemma.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把自定义拦截器注册到mvc中
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtOperator operator;

    @Bean
    public LoginInterceptor loginInterceptor(){
        return new LoginInterceptor(operator);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor()).addPathPatterns("/**");
    }
}
