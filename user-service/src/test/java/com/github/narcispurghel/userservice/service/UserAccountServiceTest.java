package com.github.narcispurghel.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.narcispurghel.userservice.dto.UserDtos;
import com.github.narcispurghel.userservice.entity.Role;
import com.github.narcispurghel.userservice.entity.User;
import com.github.narcispurghel.userservice.repository.AdopterProfileJpaRepository;
import com.github.narcispurghel.userservice.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class UserAccountServiceTest {

  private final UserJpaRepository userRepository = mock(UserJpaRepository.class);
  private final AdopterProfileJpaRepository profileRepository =
    mock(AdopterProfileJpaRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

  private final UserAccountService service =
    new UserAccountService(userRepository, profileRepository, passwordEncoder);

  @Test
  void currentUser_found_returnsView() {
    UUID id = UUID.randomUUID();
    when(userRepository.findByIdWithProfile(id))
      .thenReturn(Optional.of(user(id, "a@b.test", "hash", true)));

    UserDtos.CurrentUser view = service.currentUser(id);

    assertThat(view.id()).isEqualTo(id.toString());
    assertThat(view.email()).isEqualTo("a@b.test");
    assertThat(view.role()).isEqualTo("USER");
    assertThat(view.profile()).isNull();
  }

  @Test
  void currentUser_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(userRepository.findByIdWithProfile(id)).thenReturn(Optional.empty());

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.currentUser(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void changePassword_wrongCurrent_throws400() {
    UUID id = UUID.randomUUID();
    when(userRepository.findByIdWithProfile(id))
      .thenReturn(Optional.of(user(id, "a@b.test", "hash", true)));
    when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.changePassword(id, new UserDtos.ChangePasswordRequest("wrong", "newpassword1")),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void changePassword_correct_encodesAndSaves() {
    UUID id = UUID.randomUUID();
    User user = user(id, "a@b.test", "hash", true);
    when(userRepository.findByIdWithProfile(id)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("current", "hash")).thenReturn(true);
    when(passwordEncoder.encode("newpassword1")).thenReturn("newhash");

    service.changePassword(id, new UserDtos.ChangePasswordRequest("current", "newpassword1"));

    assertThat(user.getPasswordHash()).isEqualTo("newhash");
    verify(userRepository).save(user);
  }

  @Test
  void deactivate_wrongPassword_throws400() {
    UUID id = UUID.randomUUID();
    when(userRepository.findByIdWithProfile(id))
      .thenReturn(Optional.of(user(id, "a@b.test", "hash", true)));
    when(passwordEncoder.matches(eq("nope"), any())).thenReturn(false);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.deactivate(id, new UserDtos.DeactivateRequest("nope")),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void deactivate_correctPassword_setsInactive() {
    UUID id = UUID.randomUUID();
    User user = user(id, "a@b.test", "hash", true);
    when(userRepository.findByIdWithProfile(id)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

    service.deactivate(id, new UserDtos.DeactivateRequest("secret"));

    assertThat(user.isActive()).isFalse();
    verify(userRepository).save(user);
  }

  @Test
  void deleteUser_deletes() {
    UUID id = UUID.randomUUID();
    User user = user(id, "a@b.test", "hash", true);
    when(userRepository.findByIdWithProfile(id)).thenReturn(Optional.of(user));

    service.deleteUser(id);

    verify(userRepository).delete(user);
  }

  private static User user(UUID id, String email, String hash, boolean active) {
    User user = User.builder().email(email).passwordHash(hash).role(Role.USER).active(active).build();
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
