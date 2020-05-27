package com.dilemma.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class GatewayConfig {
    @Bean
    public RouterFunction<ServerResponse> testFunRouterFunction(){
        RouterFunction<ServerResponse> route = RouterFunctions.route(
                RequestPredicates.path("test"),serverRequest -> ServerResponse.ok()
                .body(BodyInserters.fromObject("i am testing")));
        return route;
    }
    /**
     * 配置路由规则
     * 可以通过 .route()配置多个路由规则
     * @param builder 路由规则构建器
     * @return
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
        return builder.routes().route(r -> r.path("item-service/**").uri("lb://item-service"))
                .route(r -> r.path("upload-service/**").uri("lb://upload-service"))
                .route(r -> r.path("search-service/**").uri("lb://search-service"))
                .route(r -> r.path("user-service/**").uri("lb://user-service"))
                .route(r -> r.path("auth-service/**").uri("lb://auth-service"))
                .route(r -> r.path("cart-service/**").uri("lb://cart-service"))
                .route(r -> r.path("order-service/**").uri("lb://order-service"))
                .build();
    }



}
