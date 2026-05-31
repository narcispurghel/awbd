package com.github.narcispurghel.common.jwt;

import com.github.narcispurghel.common.AwbdCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** Hashes raw JWT strings for Redis blacklist keys. */
public final class TokenBlacklistHasher {

  public static final String BLACKLIST_KEY_PREFIX = "blacklist:jwt:";

  private TokenBlacklistHasher() {}

  public static String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(token.getBytes(AwbdCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(encodedHash);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to hash token for blacklisting", e);
    }
  }

  public static String blacklistKey(String token) {
    return BLACKLIST_KEY_PREFIX + hashToken(token);
  }
}
