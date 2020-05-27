package com.dilemma.user.service;

import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.common.utils.NumberUtils;
import com.dilemma.user.mapper.UserMapper;
import com.dilemma.user.pojo.User;
import com.dilemma.user.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;



    private static final String KEY_PREFIX = "user:code:phone:";


    public Boolean checkData(String data, Integer type) {
        User record = new User();
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new MsException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return this.userMapper.selectCount(record) == 0;
    }

    public Boolean sendVerifyCode(String phone) {
        //生成6位数验证码
        String code = NumberUtils.generateCode(6);

        try {
            Map<String,String> msg = new HashMap<>();
            msg.put("phone",phone);
            msg.put("code",code);
            //消息异步调用发起对阿里云第三方接口的调用给用户发送验证码信息
            log.info("生成验证码为：{}",code);
            //TODO 异步调用阿里云短信服务验证
            //this.amqpTemplate.convertAndSend("mall.sms.exchange","sms.verify.code",msg);
            //把code存入redis，设置该验证码有效时长为5分钟
            this.redisTemplate.opsForValue().set(KEY_PREFIX + phone,code,5,TimeUnit.MINUTES);
            return true;
        } catch (AmqpException e) {
            e.printStackTrace();
            log.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }
    }

    public Boolean register(User user, String code) {
        //获取短信验证码
        String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code,cacheCode)){
            return false;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        //强制设置不能指定的参数为null
        user.setId(null);
        user.setCreated(new Date());
        //添加到数据库
        boolean b = this.userMapper.insert(user) == 1;
        if (b){
            //注册成功，并删除redis中的记录
            this.redisTemplate.delete(KEY_PREFIX+user.getPhone());
        }
        return b;
    }

    public User queryUser(String username, String password) {
        //查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        //校验用户名
        if (user == null){
            return null;
        }
        //密码校验
        if (!user.getPassword().equals(CodecUtils.md5Hex(password,user.getSalt()))){
            return null;
        }
        //返回正确结果
        return user;
    }
}
