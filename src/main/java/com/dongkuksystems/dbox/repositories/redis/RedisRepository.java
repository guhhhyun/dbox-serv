package com.dongkuksystems.dbox.repositories.redis;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class RedisRepository {
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public RedisRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void put(String key, Object value, Long expirationTime) {
    if (expirationTime != null) {
      redisTemplate.opsForValue().set(key, value, expirationTime, TimeUnit.SECONDS);
    } else {
      redisTemplate.opsForValue().set(key, value);
    }
  }

  public void put(String key, String value, Long expirationTime) {
    if (expirationTime != null) {
      redisTemplate.opsForValue().set(key, value, expirationTime, TimeUnit.SECONDS);
    } else {
      redisTemplate.opsForValue().set(key, value);
    }
  }

  public void delete(String key) {
    redisTemplate.delete(key);
  }

  public Long deleteByKeyCollection(Collection<String> keys) {
    return redisTemplate.delete(keys);
  }
  
  public <T> Object getObject(String key, Class<T> classType) {
    if (UserSession.class == classType) {
      key = Commons.SESSION_PREFIX + key;
    }
    Object rst = redisTemplate.opsForValue().get(key);
    if (rst == null) {
      return null;
    }
    T obj = objectMapper.convertValue(rst, classType);
    return obj;
  }

  @SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> classType) {
    if (UserSession.class == classType) {
      key = Commons.SESSION_PREFIX + key;
    }
    T result = (T) redisTemplate.opsForValue().get(key);
    
    return result;
  } 

  public Set<String> getKeys(String pattern) { 
    return redisTemplate.keys(pattern);
  }
  
  public boolean isExists(String key) {
    return redisTemplate.hasKey(key);
  }

  public void setExpireTime(String key, long expirationTime) {
    redisTemplate.expire(key, expirationTime, TimeUnit.SECONDS);
  }

  public long getExpireTime(String key) {
    return redisTemplate.getExpire(key, TimeUnit.SECONDS);
  }
}
