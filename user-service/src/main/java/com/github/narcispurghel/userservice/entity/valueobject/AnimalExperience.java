package com.github.narcispurghel.userservice.entity.valueobject;

import com.github.narcispurghel.userservice.entity.DurationUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import java.util.Objects;

@Embeddable
public final class AnimalExperience {

  public static final int MIN_ANIMAL_EXPERIENCE = 0;
  public static final int MAX_EXPERIENCE_YEARS = 100;
  public static final int MAX_EXPERIENCE_MONTHS = 1200;
  public static final int MAX_EXPERIENCE_WEEKS = 5200;

  @Min(MIN_ANIMAL_EXPERIENCE)
  @Column(name = "experience_amount", nullable = false)
  private final int amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "experience_unit", nullable = false)
  private final DurationUnit unit;

  protected AnimalExperience() {
    amount = MIN_ANIMAL_EXPERIENCE;
    unit = DurationUnit.MONTHS;
  }

  public AnimalExperience(int amount, DurationUnit unit) {
    if (amount < MIN_ANIMAL_EXPERIENCE) throw new IllegalArgumentException(
      "Experience amount cannot be negative"
    );
    if (
      (amount > MAX_EXPERIENCE_YEARS) & (unit == DurationUnit.YEARS)
    ) throw new IllegalArgumentException(
      "Experience amount cannot be greater than %s %s".formatted(
        MAX_EXPERIENCE_YEARS,
        DurationUnit.YEARS
      )
    );
    if (
      (amount > MAX_EXPERIENCE_MONTHS) & (unit == DurationUnit.MONTHS)
    ) throw new IllegalArgumentException(
      "Experience amount cannot be greater than %s %s".formatted(
        MAX_EXPERIENCE_MONTHS,
        DurationUnit.MONTHS
      )
    );
    if (
      (amount > MAX_EXPERIENCE_WEEKS) & (unit == DurationUnit.WEEKS)
    ) throw new IllegalArgumentException(
      "Experience amount cannot be greater than %s %s".formatted(
        MAX_EXPERIENCE_WEEKS,
        DurationUnit.WEEKS
      )
    );
    this.amount = amount;
    this.unit = unit;
  }

  public int getAmount() {
    return amount;
  }

  public DurationUnit getUnit() {
    return unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AnimalExperience e)) return false;
    return amount == e.amount && unit == e.unit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, unit);
  }

  @Override
  public String toString() {
    return amount + " " + unit.name().toLowerCase();
  }
}
