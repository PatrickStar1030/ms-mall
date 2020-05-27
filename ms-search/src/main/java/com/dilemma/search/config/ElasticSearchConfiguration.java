package com.dilemma.search.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * es 配置类，用factoryBean工厂管理
 */
@Configuration
@Slf4j
public class ElasticSearchConfiguration implements FactoryBean<RestHighLevelClient>,InitializingBean,DisposableBean {
    @Value("${spring.data.elasticsearch.host}")
    private String host;
    @Value("${spring.data.elasticsearch.port}")
    private int port;
    @Value("${spring.data.elasticsearch.scheme}")
    private String scheme;

    private RestHighLevelClient restHighLevelClient;

    //restHighLevelClient 销毁
    @Override
    public void destroy() {
        try {
            log.info("Closing elasticsearch");
            if (restHighLevelClient != null){
                restHighLevelClient.close();
            }
        } catch (Exception e) {
            log.error("error closing elasticsearch client",e);
            e.printStackTrace();
        }
    }
    //getBean
    @Override
    public RestHighLevelClient getObject() {
        return restHighLevelClient;
    }
    //getBeanType
    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }
    //初始化client配置
    @Override
    public void afterPropertiesSet()  {
        buildClient();
    }

    private void buildClient(){
        log.info("elasticsearch host :{},post:{}",host,port);
        RestClientBuilder builder = RestClient.builder(new HttpHost(host,port,scheme));
        restHighLevelClient = new RestHighLevelClient(builder);
    }
}
