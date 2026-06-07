package com.github.narcispurghel.animalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "animal_id", nullable = false)
  private Animal animal;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(name = "examination_date", nullable = false)
  private LocalDate examinationDate;

  @Nullable
  @Column(length = 255)
  private String treatment;

  @Nullable
  @Column(length = 2000)
  private String notes;

  @Nullable
  @Column(name = "weight_kg", precision = 5, scale = 2)
  private BigDecimal weightKg;

  @Column(name = "follow_up_required", nullable = false)
  private boolean followUpRequired;

  @SuppressWarnings("NullAway.Init")
  public MedicalRecord() {
    animal = new Animal();
    title = "";
    examinationDate = LocalDate.now(ZoneId.systemDefault());
  }

  public UUID getId() {
    return id;
  }

  public Animal getAnimal() {
    return animal;
  }

  public void setAnimal(Animal animal) {
    this.animal = animal;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDate getExaminationDate() {
    return examinationDate;
  }

  public void setExaminationDate(LocalDate examinationDate) {
    this.examinationDate = examinationDate;
  }

  public @Nullable String getTreatment() {
    return treatment;
  }

  public void setTreatment(@Nullable String treatment) {
    this.treatment = treatment;
  }

  public @Nullable String getNotes() {
    return notes;
  }

  public void setNotes(@Nullable String notes) {
    this.notes = notes;
  }

  public @Nullable BigDecimal getWeightKg() {
    return weightKg;
  }

  public void setWeightKg(@Nullable BigDecimal weightKg) {
    this.weightKg = weightKg;
  }

  public boolean isFollowUpRequired() {
    return followUpRequired;
  }

  public void setFollowUpRequired(boolean followUpRequired) {
    this.followUpRequired = followUpRequired;
  }
}
