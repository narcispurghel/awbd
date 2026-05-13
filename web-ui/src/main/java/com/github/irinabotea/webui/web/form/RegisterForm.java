package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank
    @Email
    @Size(max = 254)
    private String email = "";

    @NotBlank
    @Size(min = 8, max = 72)
    private String password = "";

    @NotBlank
    @Size(max = 100)
    private String firstName = "";

    @NotBlank
    @Size(max = 100)
    private String lastName = "";

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
