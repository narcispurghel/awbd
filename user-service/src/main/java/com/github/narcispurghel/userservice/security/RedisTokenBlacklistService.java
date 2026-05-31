package com.github.narcispurghel.userservice.security;

public interface RedisTokenBlacklistService {
  void addToBlacklist(String token, long ttlSeconds);

  boolean isTokenBlacklisted(String token);
}
