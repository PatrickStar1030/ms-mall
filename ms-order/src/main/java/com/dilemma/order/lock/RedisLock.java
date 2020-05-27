package com.dilemma.order.lock;

import com.dilemma.order.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import sun.font.Script;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
@Component
public class RedisLock implements Lock {
    private static final String LOCK_KEY = "lock";

    private ThreadLocal<String> local = new ThreadLocal<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    private DefaultRedisScript<List> getRedisScript;

    @PostConstruct
    public void init(){
        getRedisScript = new DefaultRedisScript<>();
        getRedisScript.setResultType(List.class);
        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
    }


    //阻塞式加锁
    @Override
    public void lock() {
        //1、尝试加锁
        if (tryLock()){
            return;
        }
        //2.加锁失败，睡眠一段时间
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //3.递归调用继续尝试加锁
        lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }
    //阻塞式加锁，使用setNx命令返回ok的加锁成功。
    @Override
    public boolean tryLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean boo = this.redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, uuid, 1, TimeUnit.SECONDS);
        assert boo != null;
        if (boo){
            local.set(uuid);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    //调用Lua脚本解锁，移除setnx key 保持原子性
    @Override
    public void unlock() {
        List<String> keyList = new ArrayList<>();
        keyList.add(LOCK_KEY);
        this.redisTemplate.execute(getRedisScript,keyList,local.get());
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
