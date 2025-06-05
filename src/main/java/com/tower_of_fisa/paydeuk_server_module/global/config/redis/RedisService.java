package com.tower_of_fisa.paydeuk_server_module.global.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate<String, String> redisTemplate;

  // 키-값 저장 (TTL을 지정할 수 있는 메서드 추가)
  public void saveValue(String key, String value, long ttl) {
    redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
  }

  public String getValue(String key) {
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? value : ""; // null 대신 빈 문자열을 반환
  }

  // Redis에서 해당 키의 값을 증가시키는 메서드
  public void addValue(String key, Double increment) {
    redisTemplate.opsForValue().increment(key, increment);
  }

  public long getRemainingSecondsUntilNextTwoMonthsFirstDay() {
    Calendar now = Calendar.getInstance();
    Calendar firstOfNextMonth = (Calendar) now.clone();
    firstOfNextMonth.add(Calendar.MONTH, 2); // 다음 달로 이동
    firstOfNextMonth.set(Calendar.DAY_OF_MONTH, 1); // 1일로 설정
    firstOfNextMonth.set(Calendar.HOUR_OF_DAY, 0);
    firstOfNextMonth.set(Calendar.MINUTE, 0);
    firstOfNextMonth.set(Calendar.SECOND, 0);
    firstOfNextMonth.set(Calendar.MILLISECOND, 0);

    long diffInMillis = firstOfNextMonth.getTimeInMillis() - now.getTimeInMillis();
    return TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
  }
}
