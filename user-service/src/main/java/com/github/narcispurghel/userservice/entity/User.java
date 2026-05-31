package com.github.narcispurghel.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Email
  @NotBlank
  @Column(unique = true, nullable = false)
  private String email;

  @NotBlank
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private final LocalDateTime createdAt = LocalDateTime.now(ZoneId.systemDefault());

  @Nullable
  @OneToOne(
    mappedBy = "user",
    cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }
  )
  private AdopterProfile profile;

  @SuppressWarnings("NullAway.Init")
  protected User() {
    email = "";
    passwordHash = "";
    role = Role.USER;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public @Nullable AdopterProfile getProfile() {
    return profile;
  }

  public void setProfile(@Nullable AdopterProfile profile) {
    this.profile = profile;
  }

  public static final class Builder {

    private @Nullable String email;
    private @Nullable String passwordHash;
    private Role role = Role.USER;
    private boolean active = true;

    private Builder() {}

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder passwordHash(String passwordHash) {
      this.passwordHash = passwordHash;
      return this;
    }

    public Builder role(Role role) {
      this.role = role;
      return this;
    }

    public Builder active(boolean active) {
      this.active = active;
      return this;
    }

    public User build() {
      User user = new User();
      user.setEmail(Objects.requireNonNull(email, "email"));
      user.setPasswordHash(Objects.requireNonNull(passwordHash, "passwordHash"));
      user.setRole(role);
      user.setActive(active);
      return user;
    }
  }
}
