package com.github.narcispurghel.userservice.entity;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public enum CountryCode {
  RO(40),
  MD(373);

  private final int dialCode;

  CountryCode(int dialCode) {
    if (dialCode < 0 || dialCode > 65535) {
      throw new IllegalArgumentException("Invalid dialCode " + dialCode);
    }
    this.dialCode = dialCode;
  }

  public int getDialCode() {
    return dialCode;
  }

  /** Returns the enum constant matching the given dial code (e.g. {@code "+40"}), or {@code null} if not found. */
  public static @Nullable CountryCode fromDialCode(int dialCode) {
    for (CountryCode cc : values()) {
      if (cc.dialCode == dialCode) return cc;
    }
    return null;
  }

  @Override
  public String toString() {
    return Objects.toString(dialCode);
  }
}
