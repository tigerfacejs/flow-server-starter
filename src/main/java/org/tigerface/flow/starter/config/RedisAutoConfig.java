package org.tigerface.flow.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;

@Configuration
//@ConditionalOnClass(RedisTemplate.class)
//@ConditionalOnMissingBean(type = "org.springframework.data.redis.connection.RedisConnectionFactory")
@Slf4j
public class RedisAutoConfig {
    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.username:default}")
    private String username;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.db:0}")
    private int db;

    @Value("${spring.redis.lettuce.pool.max-idle:16}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle:16}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-active:32}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-active:-1}")
    private long maxWait;

    @Value("${spring.redis.cluster.max-redirects:3}")
    private int MaxRedirects;

    @Value("${spring.redis.mode:standalone}")
    private String mode;

    @Value("${spring.redis.cluster.nodes:}")
    private String nodes;

    private final static String STANDALONE = "standalone";

    @Bean
    public RedisConnectionFactory redisConnectionFactory(JedisPoolConfig jedisPool) {
        JedisConnectionFactory jedisConnectionFactory;
        if (STANDALONE.equals(mode)) {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            config.setUsername(username);
            config.setPassword(password);
            config.setDatabase(db);
            jedisConnectionFactory = new JedisConnectionFactory(config);
        } else {
            RedisClusterConfiguration config = new RedisClusterConfiguration();
            config.setUsername(username);
            config.setPassword(password);
            config.setMaxRedirects(MaxRedirects);
            Arrays.stream(nodes.split(",")).forEach(
                    item ->
                            config.addClusterNode(new RedisNode(item.split(":")[0], Integer.valueOf(item.split(":")[1])))
            );
            jedisConnectionFactory = new JedisConnectionFactory(config, jedisPool);
        }
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public JedisPoolConfig jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMinIdle(minIdle);
        return jedisPoolConfig;
    }

    @Bean
    public RedisSerializer redisSerializer() {
        return new StringRedisSerializer();
    }
}
