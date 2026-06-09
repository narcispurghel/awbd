package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class BreedForm {

  @NotNull
  private @Nullable UUID speciesId;

  @NotBlank
  @Size(max = 80)
  private String name = "";

  public @Nullable UUID getSpeciesId() {
    return speciesId;
  }

  public void setSpeciesId(@Nullable UUID speciesId) {
    this.speciesId = speciesId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
