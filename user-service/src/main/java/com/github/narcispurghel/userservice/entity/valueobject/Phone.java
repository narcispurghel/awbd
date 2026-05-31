package com.github.narcispurghel.userservice.entity.valueobject;

import com.github.narcispurghel.userservice.entity.CountryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

@Embeddable
public final class Phone {

  /** E.164 format: {@code +} followed by country code and subscriber number, 8–15 digits total (e.g. {@code +40721000000}). */
  public static final String VALID_PHONE_REGEX = "^\\+[1-9][0-9]{7,14}$";

  /** Local subscriber number without country code. 7–12 digits. */
  public static final String VALID_NUMBER_REGEX = "^[0-9]{7,12}$";

  private static final String DIAL_PREFIX = "+";

  @Pattern(regexp = VALID_PHONE_REGEX, message = "Invalid phone number")
  @Column(name = "phone")
  private final String value;

  protected Phone() {
    value = "";
  }

  public Phone(CountryCode countryCode, String number) {
    if (number == null || !number.matches(VALID_NUMBER_REGEX)) {
      throw new IllegalArgumentException("Invalid phone number: " + number);
    }
    this.value = DIAL_PREFIX + countryCode.getDialCode() + number;
  }

  public static Phone fromE164(String e164) {
    if (e164 == null || !e164.matches(VALID_PHONE_REGEX)) {
      throw new IllegalArgumentException("Invalid phone number: " + e164);
    }
    return new Phone(e164);
  }

  private Phone(String e164) {
    this.value = e164;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Phone p)) return false;
    return Objects.equals(value, p.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
