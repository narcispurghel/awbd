package com.github.irinabotea.webui.web.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public class ProfileForm {

    @NotBlank
    @Size(max = 100)
    private String firstName = "";

    @NotBlank
    @Size(max = 100)
    private String lastName = "";

    @Nullable
    @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$", message = "Phone must be in E.164 format, e.g. +40712345678")
    private String phone;

    @Nullable
    @Size(max = 100)
    private String city;

    @NotBlank
    private String houseType = "APARTMENT";

    private boolean hasYard;

    @Min(0)
    private int experienceWithPets;

    private boolean verifiedStatus;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public @Nullable String getPhone() { return phone; }
    public void setPhone(@Nullable String phone) { this.phone = phone; }
    public @Nullable String getCity() { return city; }
    public void setCity(@Nullable String city) { this.city = city; }
    public String getHouseType() { return houseType; }
    public void setHouseType(String houseType) { this.houseType = houseType; }
    public boolean isHasYard() { return hasYard; }
    public void setHasYard(boolean hasYard) { this.hasYard = hasYard; }
    public int getExperienceWithPets() { return experienceWithPets; }
    public void setExperienceWithPets(int experienceWithPets) { this.experienceWithPets = experienceWithPets; }
    public boolean isVerifiedStatus() { return verifiedStatus; }
    public void setVerifiedStatus(boolean verifiedStatus) { this.verifiedStatus = verifiedStatus; }
}
