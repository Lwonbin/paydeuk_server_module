package com.tower_of_fisa.paydeuk_server_module.global.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate<String, Object> redisTemplate;

  public void saveValue(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public String getValue(String key) {
    return (String) redisTemplate.opsForValue().get(key);
  }

  public void deleteKey(String key) {
    redisTemplate.delete(key);
  }
}
