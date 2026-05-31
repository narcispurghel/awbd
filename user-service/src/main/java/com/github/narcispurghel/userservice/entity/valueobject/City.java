package com.github.narcispurghel.userservice.entity.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public final class City {

  public static final int MAX_LENGTH = 100;

  @NotBlank
  @Size(max = MAX_LENGTH)
  @Column(name = "city")
  private final String value;

  protected City() {
    value = "";
  }

  public City(String value) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException(
      "City cannot be blank"
    );
    this.value = value.strip();
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof City c)) return false;
    return Objects.equals(value, c.value);
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
