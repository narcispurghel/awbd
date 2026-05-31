package com.github.narcispurghel.userservice.entity.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public final class Name {

  public static final int MAX_LENGTH = 100;

  @NotBlank
  @Size(max = MAX_LENGTH)
  @Column(nullable = false)
  private final String value;

  private Name() {
    value = "";
  }

  public Name(String value) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException(
      "Name cannot be blank"
    );
    this.value = value.strip();
  }

  public static Name from(String value) {
    return new Name(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Name n)) return false;
    return Objects.equals(value, n.value);
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
