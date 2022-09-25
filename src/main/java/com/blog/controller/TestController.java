package com.blog.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final RedisTemplate<String, String> redisTemplate0;
    private final RedisTemplate<String, String> redisTemplate1;

    public TestController(@Qualifier("redisTemplate0") RedisTemplate<String, String> redisTemplate0,
                          @Qualifier("redisTemplate1") RedisTemplate<String, String> redisTemplate1){
        this.redisTemplate0 = redisTemplate0;
        this.redisTemplate1 = redisTemplate1;
    }

    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    public String test(){
        long time = System.currentTimeMillis();
        this.redisTemplate0.opsForValue().set("ddd", "ddd");
        this.redisTemplate1.opsForValue().set("ddd", "ddd");
        return String.valueOf(System.currentTimeMillis() - time);
    }

}
