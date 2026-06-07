package com.github.narcispurghel.animalservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "shelters")
public class Shelter {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true, length = 120)
  private String name;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @Column(name = "contact_email", nullable = false, length = 255)
  private String contactEmail;

  @Nullable
  @Column(name = "contact_phone", length = 30)
  private String contactPhone;

  @Nullable
  @Column(length = 255)
  private String address;

  @SuppressWarnings("NullAway.Init")
  public Shelter() {
    name = "";
    city = "";
    countryCode = "";
    contactEmail = "";
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
