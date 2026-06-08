package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public class ShelterForm {

  @NotBlank
  @Size(max = 120)
  private String name = "";

  @NotBlank
  @Size(max = 100)
  private String city = "";

  @NotBlank
  @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be 2 uppercase letters (ISO 3166-1 alpha-2)")
  private String countryCode = "";

  @NotBlank
  @Email
  @Size(max = 255)
  private String contactEmail = "";

  @Nullable
  @Size(max = 30)
  private String contactPhone;

  @Nullable
  @Size(max = 255)
  private String address;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public @Nullable String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(@Nullable String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public @Nullable String getAddress() {
    return address;
  }

  public void setAddress(@Nullable String address) {
    this.address = address;
  }
}
