package com.dongkuksystems.dbox.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@Configuration
@EnableCaching
public class RedisConfigure extends CachingConfigurerSupport {

  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private int redisPort;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(redisHost, redisPort) ;
  }
  
  @Bean
  public GenericJackson2JsonRedisSerializer jacksonRedisSerializer(Jackson2ObjectMapperBuilder jacksonBuilder) {
  	ObjectMapper objectMapper = jacksonBuilder.build();
  	objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);	// 자료형과 함께 시리얼라이즈 설정
  	GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

  	return serializer;
  }
  
  @Bean
  public RedisTemplate<String, Object> redisTemplate(GenericJackson2JsonRedisSerializer jacksonRedisSerializer) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>(); 
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    redisTemplate.setDefaultSerializer(jacksonRedisSerializer);

    return redisTemplate;
  }
  
}

