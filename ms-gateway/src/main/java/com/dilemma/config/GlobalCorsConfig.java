package com.dilemma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;



public class GlobalCorsConfig {
    public CorsWebFilter corsFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1.允许访问的域
        corsConfiguration.addAllowedOrigin("http://manage.ms-mall.com");
        corsConfiguration.addAllowedOrigin("http://www.ms-mall.com");
        //2.是否发送cookie信息
        corsConfiguration.setAllowCredentials(true);
        //3.允许的请求方式
        corsConfiguration.addAllowedMethod("OPTIONS");
        corsConfiguration.addAllowedMethod("HEAD");
        corsConfiguration.addAllowedMethod("GET");
        corsConfiguration.addAllowedMethod("PUT");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedMethod("DELETE");
        corsConfiguration.addAllowedMethod("PATCH");
        //4.允许的头信息
        corsConfiguration.addAllowedHeader("*");
        //5.设置有效时长
        corsConfiguration.setMaxAge(3600L);
        //2、添加映射路径，拦截所有的请求
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(configurationSource);
    }
}
