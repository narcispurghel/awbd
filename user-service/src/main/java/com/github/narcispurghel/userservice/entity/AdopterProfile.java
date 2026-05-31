package com.github.narcispurghel.userservice.entity;

import com.github.narcispurghel.userservice.entity.valueobject.AnimalExperience;
import com.github.narcispurghel.userservice.entity.valueobject.City;
import com.github.narcispurghel.userservice.entity.valueobject.Name;
import com.github.narcispurghel.userservice.entity.valueobject.Phone;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "adopter_profile")
public class AdopterProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "last_name", nullable = false))
  private Name lastName;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "first_name", nullable = false))
  private Name firstName;

  @Nullable
  @Embedded
  private Phone phone;

  @Nullable
  @Embedded
  private City city;

  @Enumerated(EnumType.STRING)
  @Column(name = "house_type", nullable = false)
  private HouseType houseType;

  @Column(name = "has_yard", nullable = false)
  private boolean hasYard;

  @Nullable
  @Embedded
  private AnimalExperience animalExperience;

  @Enumerated(EnumType.STRING)
  @Column(name = "verification_status", nullable = false)
  private VerificationStatus verificationStatus = VerificationStatus.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private final LocalDateTime createdAt = LocalDateTime.now(ZoneId.systemDefault());

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @SuppressWarnings("NullAway.Init")
  protected AdopterProfile() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Name getLastName() {
    return lastName;
  }

  public void setLastName(Name lastName) {
    this.lastName = lastName;
  }

  public Name getFirstName() {
    return firstName;
  }

  public void setFirstName(Name firstName) {
    this.firstName = firstName;
  }

  public @Nullable Phone getPhone() {
    return phone;
  }

  public void setPhone(@Nullable Phone phone) {
    this.phone = phone;
  }

  public @Nullable City getCity() {
    return city;
  }

  public void setCity(@Nullable City city) {
    this.city = city;
  }

  public HouseType getHouseType() {
    return houseType;
  }

  public void setHouseType(HouseType houseType) {
    this.houseType = houseType;
  }

  public boolean isHasYard() {
    return hasYard;
  }

  public void setHasYard(boolean hasYard) {
    this.hasYard = hasYard;
  }

  public @Nullable AnimalExperience getAnimalExperience() {
    return animalExperience;
  }

  public void setAnimalExperience(@Nullable AnimalExperience animalExperience) {
    this.animalExperience = animalExperience;
  }

  public VerificationStatus getVerificationStatus() {
    return verificationStatus;
  }

  public void setVerificationStatus(VerificationStatus verificationStatus) {
    this.verificationStatus = verificationStatus;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public static final class Builder {

    private @Nullable User user;
    private @Nullable Name firstName;
    private @Nullable Name lastName;
    private HouseType houseType = HouseType.APARTMENT;
    private boolean hasYard;
    private @Nullable AnimalExperience animalExperience;
    private LocalDateTime updatedAt = LocalDateTime.now(ZoneId.systemDefault());

    private Builder() {}

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public Builder firstName(Name firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(Name lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder houseType(HouseType houseType) {
      this.houseType = houseType;
      return this;
    }

    public Builder hasYard(boolean hasYard) {
      this.hasYard = hasYard;
      return this;
    }

    public Builder animalExperience(AnimalExperience animalExperience) {
      this.animalExperience = animalExperience;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public AdopterProfile build() {
      AdopterProfile profile = new AdopterProfile();
      profile.setUser(Objects.requireNonNull(user, "user"));
      profile.setFirstName(Objects.requireNonNull(firstName, "firstName"));
      profile.setLastName(Objects.requireNonNull(lastName, "lastName"));
      profile.setHouseType(houseType);
      profile.setHasYard(hasYard);
      profile.setAnimalExperience(Objects.requireNonNull(animalExperience, "animalExperience"));
      profile.setUpdatedAt(updatedAt);
      return profile;
    }
  }
}
