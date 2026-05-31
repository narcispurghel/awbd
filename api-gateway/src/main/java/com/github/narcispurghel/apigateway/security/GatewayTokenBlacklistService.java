package com.github.narcispurghel.apigateway.security;

import com.github.narcispurghel.common.jwt.TokenBlacklistHasher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GatewayTokenBlacklistService {

  private final RedisTemplate<String, String> redisTemplate;

  public GatewayTokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public boolean isBlacklisted(String token) {
    if (token.isBlank()) {
      return false;
    }
    return Boolean.TRUE.equals(redisTemplate.hasKey(TokenBlacklistHasher.blacklistKey(token)));
  }
}
