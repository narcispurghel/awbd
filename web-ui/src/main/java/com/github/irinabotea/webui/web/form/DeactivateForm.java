package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public class DeactivateForm {

  @NotBlank
  private String password = "";

  @AssertTrue(message = "You must confirm to deactivate your account")
  private boolean confirm;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isConfirm() {
    return confirm;
  }

  public void setConfirm(boolean confirm) {
    this.confirm = confirm;
  }
}
