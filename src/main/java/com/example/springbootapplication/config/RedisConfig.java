package com.example.springbootapplication.config;

import com.example.springbootapplication.util.CustomCacheErrorHandler;
import com.example.springbootapplication.util.CustomRedisSerializer;
import io.lettuce.core.ReadFrom;
import lombok.Getter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@Getter
public class RedisConfig implements CachingConfigurer {

  @Value("${default.redis.connection:standalone}")
  private String connectionType;

  @Value("${default.redis.TTL:60}")
  private Integer timeToLive;

  @Autowired
  GenericObjectPoolConfig genericObjectPoolConfig;

  @Autowired
  private RedisProperties redisProperties;

  @Autowired
  private ConnectionPoolProperties poolConfig;

  /**
   * @Bean(name = "defaultRedisTemplate") public RedisTemplate<Object, Object>
   * redisTemplate(RedisConnectionFactory connectionFactory) { RedisTemplate<Object, Object>
   * template = new RedisTemplate<>(); template.setConnectionFactory(connectionFactory); // Add some
   * specific configuration here. Key serializers, etc. return template; }
   */

  @Bean
  public JedisClientConfiguration poolingClientConfiguration() {
    return JedisClientConfiguration.builder()
            .usePooling()
            .poolConfig(genericObjectPoolConfig)
            .build();
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new CustomCacheErrorHandler();
  }

  @Bean(name = "customConnectionFactory")
  public RedisConnectionFactory connectionFactory(
          JedisClientConfiguration jedisClientConfiguration) {
    RedisConnectionFactory redisConnectionFactory;
    if (connectionType.equalsIgnoreCase("standalone")) {
      redisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration(), jedisClientConfiguration);
    } else {
      redisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration(), jedisClientConfiguration);
    }
    return redisConnectionFactory;
  }

  @Override
  @Bean
  public RedisCacheManager cacheManager() {
    return RedisCacheManager
        .builder(this.connectionFactory(poolingClientConfiguration()))
        .cacheDefaults(this.cacheConfiguration())
        .build();
  }

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(timeToLive))
        .disableCachingNullValues()
        .serializeValuesWith(RedisSerializationContext
            .SerializationPair
            .fromSerializer(new CustomRedisSerializer()));
  }

  @Bean(name = "customRedisTemplate")
  public RedisTemplate<Object, Object> customRedisTemplate(
      @Qualifier("customConnectionFactory") RedisConnectionFactory connectionFactory) {

    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setDefaultSerializer(new CustomRedisSerializer());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new CustomRedisSerializer());
    redisTemplate.setHashValueSerializer(new CustomRedisSerializer());
    return redisTemplate;
  }

  private RedisStandaloneConfiguration redisStandaloneConfiguration() {

    RedisStandaloneConfiguration redisStandaloneConfiguration
        = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());

    if (StringUtils.hasText(redisProperties.getPassword())) {
      redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
    }

    return redisStandaloneConfiguration;
  }

  private RedisSentinelConfiguration redisSentinelConfiguration() {


    RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
        .master(redisProperties.getSentinel().getMaster());

    redisProperties.getSentinel().getNodes().forEach(s -> sentinelConfig.sentinel(s.split(":")[0],
        Integer.valueOf(s.split(":")[1])
    ));

    if (StringUtils.hasText(redisProperties.getPassword())) {
      sentinelConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
    }

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .commandTimeout(redisProperties.getTimeout())
        .readFrom(ReadFrom.REPLICA_PREFERRED)
        .build();

    return sentinelConfig;
  }
}
