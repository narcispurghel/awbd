package com.github.irinabotea.webui.web.form;

import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

public class AnimalForm {

  @NotBlank
  @Size(max = 120)
  private String name = "";

  @NotNull
  private @Nullable UUID shelterId;

  @NotNull
  private @Nullable UUID speciesId;

  private @Nullable UUID breedId;

  @NotNull
  private AnimalDtos.AnimalStatus status = AnimalDtos.AnimalStatus.INTAKE;

  @NotNull
  private AnimalDtos.Sex sex = AnimalDtos.Sex.UNKNOWN;

  @Size(max = 2000)
  private @Nullable String description;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private @Nullable LocalDate birthDate;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private @Nullable LocalDate intakeDate = LocalDate.now();

  @DecimalMin("0.0")
  private @Nullable BigDecimal adoptionFee;

  private boolean vaccinated;

  private boolean neutered;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public @Nullable UUID getShelterId() { return shelterId; }
  public void setShelterId(@Nullable UUID shelterId) { this.shelterId = shelterId; }

  public @Nullable UUID getSpeciesId() { return speciesId; }
  public void setSpeciesId(@Nullable UUID speciesId) { this.speciesId = speciesId; }

  public @Nullable UUID getBreedId() { return breedId; }
  public void setBreedId(@Nullable UUID breedId) { this.breedId = breedId; }

  public AnimalDtos.AnimalStatus getStatus() { return status; }
  public void setStatus(AnimalDtos.AnimalStatus status) { this.status = status; }

  public AnimalDtos.Sex getSex() { return sex; }
  public void setSex(AnimalDtos.Sex sex) { this.sex = sex; }

  public @Nullable String getDescription() { return description; }
  public void setDescription(@Nullable String description) { this.description = description; }

  public @Nullable LocalDate getBirthDate() { return birthDate; }
  public void setBirthDate(@Nullable LocalDate birthDate) { this.birthDate = birthDate; }

  public @Nullable LocalDate getIntakeDate() { return intakeDate; }
  public void setIntakeDate(@Nullable LocalDate intakeDate) { this.intakeDate = intakeDate; }

  public @Nullable BigDecimal getAdoptionFee() { return adoptionFee; }
  public void setAdoptionFee(@Nullable BigDecimal adoptionFee) { this.adoptionFee = adoptionFee; }

  public boolean isVaccinated() { return vaccinated; }
  public void setVaccinated(boolean vaccinated) { this.vaccinated = vaccinated; }

  public boolean isNeutered() { return neutered; }
  public void setNeutered(boolean neutered) { this.neutered = neutered; }
}
