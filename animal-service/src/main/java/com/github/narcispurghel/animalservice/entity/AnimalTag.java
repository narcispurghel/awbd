package com.github.narcispurghel.animalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "animal_tags")
public class AnimalTag {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true, length = 80)
  private String name;

  @SuppressWarnings("NullAway.Init")
  public AnimalTag() {
    name = "";
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
}
