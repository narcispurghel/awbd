package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagForm {

  @NotBlank
  @Size(max = 80)
  private String name = "";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
