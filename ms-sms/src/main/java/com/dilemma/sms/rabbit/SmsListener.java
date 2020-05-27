package com.dilemma.sms.rabbit;

import com.aliyuncs.CommonResponse;
import com.aliyuncs.exceptions.ClientException;
import com.dilemma.sms.config.SmsProperties;
import com.dilemma.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信发送监听器
 */
@Component
@EnableConfigurationProperties(SmsProperties.class)
@Slf4j
public class SmsListener {
    @Autowired
    private SmsUtils utils;
    @Autowired
    private SmsProperties smsProperties;

    /**
     *
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mall.sms.queue",durable = "true"),
            exchange = @Exchange(value = "mall.sms.exchange",ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}
    ))
    public void listenSms(Map<String,String> msg) {
        if (msg == null || msg.size() <= 0){
            return;
        }
        String phoneNumber = msg.get("phone");
        String code = msg.get("code");
        log.info("收到电话号码为：{}",phoneNumber);
        log.info("接收code为：{}",code);
        if (StringUtils.isBlank(phoneNumber)||StringUtils.isBlank(code)){
            //放弃处理
            return;
        }
        this.utils.sendSms(phoneNumber, code, smsProperties.getSignName(), smsProperties.getVerifyCodeTemplate());
    }

}
