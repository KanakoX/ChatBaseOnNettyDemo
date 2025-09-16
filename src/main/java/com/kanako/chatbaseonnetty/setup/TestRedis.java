package com.kanako.chatbaseonnetty.setup;

import com.kanako.chatbaseonnetty.user.pojo.po.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class TestRedis implements ApplicationRunner {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        UserPO userPO = new UserPO();
        userPO.setUsername("kanako");
        userPO.setPassword("123456");
//        ops.set("kanako", userPO);
    }
}
