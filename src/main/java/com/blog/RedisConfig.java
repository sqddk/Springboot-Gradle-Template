package com.blog;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.database.db0}")
    private int db0;

    @Value("${spring.redis.database.db1}")
    private int db1;

    @Value("${spring.redis.database.db2}")
    private int db2;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    private int maxWait;

    @Bean(name = "redisTemplate0")
    public RedisTemplate<String, String> createRedisTemplate0(){
        return this.createRedisTemplate(this.createRedisConnectionFactory(this.db0));
    }

    @Bean(name = "redisTemplate1")
    public RedisTemplate<String, String> createRedisTemplate1(){
        return this.createRedisTemplate(this.createRedisConnectionFactory(this.db1));
    }

    @Bean(name = "redisTemplate2")
    public RedisTemplate<String, String> createRedisTemplate2(){
        return this.createRedisTemplate(this.createRedisConnectionFactory(this.db2));
    }

    private RedisConnectionFactory createRedisConnectionFactory(int db){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(this.host);
        configuration.setPort(this.port);
        configuration.setDatabase(db);
        if (!ObjectUtils.isEmpty(this.password)) {
            RedisPassword redisPassword = RedisPassword.of(this.password);
            configuration.setPassword(redisPassword);
        }
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxTotal(this.maxActive);
        genericObjectPoolConfig.setMinIdle(this.minIdle);
        genericObjectPoolConfig.setMaxIdle(this.maxIdle);
        genericObjectPoolConfig.setMaxWait(Duration.ofMillis(this.maxWait));
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig)
                .clientResources(this.clientResources())
                .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration, clientConfig);
        factory.afterPropertiesSet();
        return factory;
    }

    private RedisTemplate<String, String> createRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    private ClientResources clientResources(){
        NettyCustomizer nettyCustomizer = new NettyCustomizer() {
            @Override
            public void afterChannelInitialized(Channel channel) {
                channel.pipeline().addLast(new IdleStateHandler(40, 0, 0));
                channel.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                        if (evt instanceof IdleStateEvent) {
                            ctx.disconnect();
                        }
                    }
                });
            }
            @Override
            public void afterBootstrapInitialized(Bootstrap bootstrap) {}
        };
        return ClientResources.builder().nettyCustomizer(nettyCustomizer).build();
    }

}
