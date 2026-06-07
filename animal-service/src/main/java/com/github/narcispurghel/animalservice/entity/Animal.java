package com.github.narcispurghel.animalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "animals")
public class Animal {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, length = 120)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shelter_id", nullable = false)
  private Shelter shelter;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "species_id", nullable = false)
  private Species species;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "breed_id")
  private Breed breed;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AnimalStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Sex sex;

  @Nullable
  @Column(length = 2000)
  private String description;

  @Nullable
  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "intake_date", nullable = false)
  private LocalDate intakeDate;

  @Nullable
  @Column(name = "adoption_fee", precision = 10, scale = 2)
  private BigDecimal adoptionFee;

  @Column(nullable = false)
  private boolean vaccinated;

  @Column(nullable = false)
  private boolean neutered;

  @SuppressWarnings("NullAway.Init")
  public Animal() {
    name = "";
    shelter = new Shelter();
    species = new Species();
    status = AnimalStatus.INTAKE;
    sex = Sex.UNKNOWN;
    intakeDate = LocalDate.now(ZoneId.systemDefault());
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Shelter getShelter() {
    return shelter;
  }

  public void setShelter(Shelter shelter) {
    this.shelter = shelter;
  }

  public Species getSpecies() {
    return species;
  }

  public void setSpecies(Species species) {
    this.species = species;
  }

  public @Nullable Breed getBreed() {
    return breed;
  }

  public void setBreed(@Nullable Breed breed) {
    this.breed = breed;
  }

  public AnimalStatus getStatus() {
    return status;
  }

  public void setStatus(AnimalStatus status) {
    this.status = status;
  }

  public Sex getSex() {
    return sex;
  }

  public void setSex(Sex sex) {
    this.sex = sex;
  }

  public @Nullable String getDescription() {
    return description;
  }

  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public @Nullable LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(@Nullable LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public LocalDate getIntakeDate() {
    return intakeDate;
  }

  public void setIntakeDate(LocalDate intakeDate) {
    this.intakeDate = intakeDate;
  }

  public @Nullable BigDecimal getAdoptionFee() {
    return adoptionFee;
  }

  public void setAdoptionFee(@Nullable BigDecimal adoptionFee) {
    this.adoptionFee = adoptionFee;
  }

  public boolean isVaccinated() {
    return vaccinated;
  }

  public void setVaccinated(boolean vaccinated) {
    this.vaccinated = vaccinated;
  }

  public boolean isNeutered() {
    return neutered;
  }

  public void setNeutered(boolean neutered) {
    this.neutered = neutered;
  }
}
