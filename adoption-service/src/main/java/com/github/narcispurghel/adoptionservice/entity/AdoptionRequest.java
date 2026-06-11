package com.github.narcispurghel.adoptionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "adoption_requests")
public class AdoptionRequest {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "animal_id", nullable = false)
  private UUID animalId;

  @Column(name = "adopter_id", nullable = false)
  private UUID adopterId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AdoptionRequestStatus status;

  @Nullable
  @Column(name = "reviewed_by")
  private UUID reviewedBy;

  @Nullable
  @Column(name = "review_note", length = 2000)
  private String reviewNote;

  @Nullable
  @Column(length = 1000)
  private String note;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @SuppressWarnings("NullAway.Init")
  public AdoptionRequest() {
    animalId = new UUID(0L, 0L);
    adopterId = new UUID(0L, 0L);
    status = AdoptionRequestStatus.PENDING;
    createdAt = Instant.EPOCH;
    updatedAt = Instant.EPOCH;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    touch();
  }

  public UUID getId() {
    return id;
  }

  public UUID getAnimalId() {
    return animalId;
  }

  public void setAnimalId(UUID animalId) {
    this.animalId = animalId;
  }

  public UUID getAdopterId() {
    return adopterId;
  }

  public void setAdopterId(UUID adopterId) {
    this.adopterId = adopterId;
  }

  public AdoptionRequestStatus getStatus() {
    return status;
  }

  public void setStatus(AdoptionRequestStatus status) {
    this.status = status;
  }

  public @Nullable UUID getReviewedBy() {
    return reviewedBy;
  }

  public void setReviewedBy(@Nullable UUID reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  public @Nullable String getReviewNote() {
    return reviewNote;
  }

  public void setReviewNote(@Nullable String reviewNote) {
    this.reviewNote = reviewNote;
  }

  public @Nullable String getNote() {
    return note;
  }

  public void setNote(@Nullable String note) {
    this.note = note;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void touch() {
    updatedAt = Instant.now();
  }
}
