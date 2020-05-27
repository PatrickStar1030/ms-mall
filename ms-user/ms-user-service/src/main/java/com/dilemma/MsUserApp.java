package com.dilemma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

@EnableEurekaClient
@SpringBootApplication
@MapperScan("com.dilemma.user.mapper")
public class MsUserApp {
    public static void main(String[] args) {
        SpringApplication.run(MsUserApp.class,args);
    }
}
