package com.dilemma.order.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mall.worker")
@Data
@AllArgsConstructor
public class IdWorkProperties {

    private long workerId;// 当前机器id

    private long dataCenterId;// 序列号
}
