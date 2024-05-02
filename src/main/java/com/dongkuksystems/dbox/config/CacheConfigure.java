package com.dongkuksystems.dbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfigure {
	private final RedisConnectionFactory connectionFactory;

	public CacheConfigure(RedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Bean
	public RedisCacheManager redisCacheManager(GenericJackson2JsonRedisSerializer jacksonRedisSerializer) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonRedisSerializer));

		RedisCacheManager redisCacheManager = RedisCacheManager.RedisCacheManagerBuilder
				.fromConnectionFactory(connectionFactory).cacheDefaults(redisCacheConfiguration).build();
		return redisCacheManager;
	}

//  @Scheduled(cron = "*/40 * * * * *")
////  @Scheduled(cron = "0 26 9 * * *", zone="Asia/Seoul")
////  @Scheduled(cron = "0 30 6 * * *")
//  public void evictAllcachesAtIntervals() {
////    CommonUtils.evictAllCaches2(redisCacheManager);
////    cacheService.clearCaches();
//  }
}
