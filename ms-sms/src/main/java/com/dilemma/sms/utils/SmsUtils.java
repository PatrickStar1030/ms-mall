package com.dilemma.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.dilemma.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class SmsUtils {

    @Autowired
    private SmsProperties smsProperties;

    //产品名称:云通信短信API产品,开发者无需替换
    static final String product = "Dysmsapi";

    //产品名称:云通信短信API产品,开发者无需替换
    static final String domain = "dysmsapi.aliyuncs.com";


    public CommonResponse sendSms(String phone, String code, String signName, String template) {
        //TODO 用redis限流
        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化DefaultProfile
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                smsProperties.getAccessKeyId(),
                smsProperties.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);


//        profile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(domain);
        request.setVersion("2020-04-06");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        request.putQueryParameter("SendDate", sdf.format(new Date()));
        //设置模板变量，自定义设计名称
        request.putQueryParameter("TemplateCode", template);
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");
        CommonResponse response = null;
        try {
            response = client.getCommonResponse(request);
            log.info("发送短信消息：{}", response.getData());
        } catch (ClientException e) {
            log.error("发送短信失败：{}",e);
            e.printStackTrace();
        }
        return response;
    }

}
