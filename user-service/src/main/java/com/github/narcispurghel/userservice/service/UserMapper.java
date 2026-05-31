package com.github.narcispurghel.userservice.service;

import com.github.narcispurghel.userservice.dto.UserDtos;
import com.github.narcispurghel.userservice.entity.AdopterProfile;
import com.github.narcispurghel.userservice.entity.DurationUnit;
import com.github.narcispurghel.userservice.entity.User;
import com.github.narcispurghel.userservice.entity.VerificationStatus;
import com.github.narcispurghel.userservice.entity.valueobject.AnimalExperience;
import org.jspecify.annotations.Nullable;

final class UserMapper {

  private UserMapper() {}

  static UserDtos.CurrentUser toCurrentUser(User user) {
    return new UserDtos.CurrentUser(
      user.getId().toString(),
      user.getEmail(),
      user.getRole().name(),
      user.isActive(),
      user.getCreatedAt().toString(),
      toProfileView(user.getProfile())
    );
  }

  static UserDtos.@Nullable ProfileView toProfileView(@Nullable AdopterProfile profile) {
    if (profile == null) {
      return null;
    }
    int experience = 0;
    if (profile.getAnimalExperience() != null) {
      experience = profile.getAnimalExperience().getAmount();
    }
    return new UserDtos.ProfileView(
      profile.getFirstName().getValue(),
      profile.getLastName().getValue(),
      profile.getPhone() == null ? null : profile.getPhone().getValue(),
      profile.getCity() == null ? null : profile.getCity().getValue(),
      profile.getHouseType().name(),
      profile.isHasYard(),
      experience,
      profile.getVerificationStatus() == VerificationStatus.APPROVED
    );
  }

  static AnimalExperience toExperience(int experienceWithPets) {
    return new AnimalExperience(experienceWithPets, DurationUnit.MONTHS);
  }
}
