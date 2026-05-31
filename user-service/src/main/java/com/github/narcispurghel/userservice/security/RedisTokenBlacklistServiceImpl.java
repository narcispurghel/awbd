package com.github.narcispurghel.userservice.security;

import com.github.narcispurghel.common.jwt.TokenBlacklistHasher;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTokenBlacklistServiceImpl implements RedisTokenBlacklistService {

  private static final String BLACKLISTED_STATUS = "BLACKLISTED";

  private final RedisTemplate<String, String> redisTemplate;

  public RedisTokenBlacklistServiceImpl(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void addToBlacklist(String token, long ttlSeconds) {
    if (token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    if (ttlSeconds <= 0) {
      throw new IllegalArgumentException("TTL must be greater than zero");
    }
    String key = TokenBlacklistHasher.blacklistKey(token);
    redisTemplate.opsForValue().set(key, BLACKLISTED_STATUS, ttlSeconds, TimeUnit.SECONDS);
  }

  @Override
  public boolean isTokenBlacklisted(String token) {
    if (token.trim().isEmpty()) {
      return false;
    }
    String key = TokenBlacklistHasher.blacklistKey(token);
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
