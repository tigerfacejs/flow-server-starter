package org.tigerface.flow.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
//@ConditionalOnClass(RedisTemplate.class)
//@ConditionalOnMissingBean(type = "org.springframework.data.redis.connection.RedisConnectionFactory")
@Slf4j
public class RedisAutoConfig {

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisSerializer redisSerializer() {
        return new StringRedisSerializer();
    }
}
