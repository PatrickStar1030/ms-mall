package com.dilemma.order.config;

import com.dilemma.common.utils.IdWorker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(IdWorkProperties.class)
@Configuration
public class IdWorkConfig {

    @Bean
    public IdWorker idWorker(IdWorkProperties properties){
        return new IdWorker(properties.getWorkerId(),properties.getDataCenterId());
    }
}
