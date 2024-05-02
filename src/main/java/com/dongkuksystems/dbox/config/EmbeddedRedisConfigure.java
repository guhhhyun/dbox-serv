package com.dongkuksystems.dbox.config;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

@Slf4j
@Profile("default")
@Configuration
public class EmbeddedRedisConfigure {

	@Value("${spring.redis.port}")
	private int redisPort;

	private RedisServer redisServer;

  @PostConstruct
  public void startRedis() {
  	try {
  		redisServer = RedisServer.builder().port(redisPort)
  				.setting("maxmemory 128M")
  				.build();
  		redisServer.start();
  		
  		log.debug("Embedded Redis (port: " + redisPort + ") started");
  	} catch (Exception e) {
  		String errorMessage = e.getMessage();
  		if (errorMessage != null) {
  			if (errorMessage.contains("Can't start redis server")) {
  	  		log.warn(errorMessage);
  			} else {
  	  		log.error(ExceptionUtils.getStackTrace(e));
  			}
  		}
  	}
  }

  @PreDestroy
  public void stopRedis() throws InterruptedException {
		Optional.ofNullable(redisServer).ifPresent(RedisServer::stop);
  }
}