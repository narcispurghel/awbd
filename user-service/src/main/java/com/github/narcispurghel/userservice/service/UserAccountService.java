package com.github.narcispurghel.userservice.service;

import com.github.narcispurghel.userservice.dto.UserDtos;
import com.github.narcispurghel.userservice.entity.AdopterProfile;
import com.github.narcispurghel.userservice.entity.HouseType;
import com.github.narcispurghel.userservice.entity.User;
import com.github.narcispurghel.userservice.entity.valueobject.City;
import com.github.narcispurghel.userservice.entity.valueobject.Name;
import com.github.narcispurghel.userservice.entity.valueobject.Phone;
import com.github.narcispurghel.userservice.repository.AdopterProfileJpaRepository;
import com.github.narcispurghel.userservice.repository.UserJpaRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAccountService {

  private final UserJpaRepository userRepository;
  private final AdopterProfileJpaRepository profileRepository;
  private final PasswordEncoder passwordEncoder;

  public UserAccountService(
    UserJpaRepository userRepository,
    AdopterProfileJpaRepository profileRepository,
    PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public UserDtos.CurrentUser currentUser(UUID userId) {
    return UserMapper.toCurrentUser(requireUser(userId));
  }

  @Transactional(readOnly = true)
  public UserDtos.CurrentUser userById(UUID userId) {
    return UserMapper.toCurrentUser(requireUser(userId));
  }

  @Transactional
  public UserDtos.ProfileView updateProfile(UUID userId, UserDtos.UpdateProfileRequest request) {
    User user = requireUser(userId);
    AdopterProfile profile = user.getProfile();
    if (profile == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
    }
    profile.setFirstName(Name.from(request.firstName()));
    profile.setLastName(Name.from(request.lastName()));
    profile.setPhone(request.phone() == null ? null : Phone.fromE164(request.phone()));
    profile.setCity(request.city() == null ? null : new City(request.city()));
    profile.setHouseType(HouseType.valueOf(request.houseType()));
    profile.setHasYard(request.hasYard());
    profile.setAnimalExperience(UserMapper.toExperience(request.experienceWithPets()));
    profile.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
    profileRepository.save(profile);
    return Objects.requireNonNull(UserMapper.toProfileView(profile));
  }

  @Transactional
  public void changePassword(UUID userId, UserDtos.ChangePasswordRequest request) {
    User user = requireUser(userId);
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
    user.setPasswordHash(Objects.requireNonNull(passwordEncoder.encode(request.newPassword())));
    userRepository.save(user);
  }

  @Transactional
  public void deactivate(UUID userId, UserDtos.DeactivateRequest request) {
    User user = requireUser(userId);
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is incorrect");
    }
    user.setActive(false);
    userRepository.save(user);
  }

  @Transactional
  public void deleteUser(UUID userId) {
    User user = requireUser(userId);
    userRepository.delete(user);
  }

  private User requireUser(UUID userId) {
    return userRepository
      .findByIdWithProfile(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
