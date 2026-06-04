package com.zee.courseauthdemo.config;


import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;
import java.time.Duration;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final Environment env;

    @Bean(name = "connectionFactory")
    RedisConnectionFactory sentinelConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        JedisClientConfiguration jedisConfig = baseJedisClientConfig()
                .connectTimeout(Duration.ofMillis(env.getProperty("redis.connection-timeout", Integer.class, 5000)))
                .readTimeout(Duration.ofMillis(env.getProperty("redis.read-timeout", Integer.class, 5000)))
                .usePooling()
                .poolConfig(jedisPoolConfig)
                .and()
                .build();

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(env.getProperty("redis.host", "localhost"));
        configuration.setPort(env.getProperty("redis.port", Integer.class, 6379));
        configuration.setDatabase(env.getProperty("redis.database-index", Integer.class,0));

        String nodeUsername = env.getProperty("redis.username", String.class,"");
        String nodePassword = env.getProperty("redis.password", String.class,"");
        if(StringUtils.isNotBlank(nodeUsername) && StringUtils.isNotBlank(nodePassword)){
            configuration.setUsername(nodeUsername);
            configuration.setPassword(nodePassword);
        }
        return new JedisConnectionFactory(configuration, jedisConfig);
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(env.getProperty("redis.max.total", Integer.class, 10));
        config.setMaxIdle(env.getProperty("redis.max.idle", Integer.class, 2));
        return config;
    }

    private JedisClientConfiguration.JedisClientConfigurationBuilder baseJedisClientConfig(){
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        boolean redisUseSsl = env.getProperty("redis.use-ssl", Boolean.class, false);

        if(redisUseSsl){
            builder
                    .useSsl();
        }
        return builder;
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory){
        return RedisCacheManager.create(redisConnectionFactory);
    }
}
