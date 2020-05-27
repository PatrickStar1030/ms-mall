package com.dilemma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan(basePackages = "com.dilemma.item.mapper")
public class MsItemApp {
    public static void main(String[] args) {
        SpringApplication.run(MsItemApp.class,args);
    }
}
