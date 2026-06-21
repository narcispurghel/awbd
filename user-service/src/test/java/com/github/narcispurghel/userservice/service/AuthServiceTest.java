package com.github.narcispurghel.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.narcispurghel.userservice.dto.AuthDtos;
import com.github.narcispurghel.userservice.entity.Role;
import com.github.narcispurghel.userservice.entity.User;
import com.github.narcispurghel.userservice.repository.UserJpaRepository;
import com.github.narcispurghel.userservice.security.JwtTokenProvider;
import com.github.narcispurghel.userservice.security.RedisTokenBlacklistService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {

  private final UserJpaRepository userRepository = mock(UserJpaRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
  private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
  private final RedisTokenBlacklistService blacklistService =
    mock(RedisTokenBlacklistService.class);

  private final AuthService service =
    new AuthService(
      userRepository,
      passwordEncoder,
      authenticationManager,
      jwtTokenProvider,
      blacklistService
    );

  @Test
  void register_newEmail_returnsResponse() {
    UUID id = UUID.randomUUID();
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password1")).thenReturn("hash");
    when(userRepository.save(any(User.class)))
      .thenReturn(user(id, "john@example.com"));

    AuthDtos.RegisterResponse response = service.register(
      new AuthDtos.RegisterRequest("john@example.com", "password1", "John", "Doe")
    );

    assertThat(response.id()).isEqualTo(id.toString());
    assertThat(response.email()).isEqualTo("john@example.com");
  }

  @Test
  void register_duplicateEmail_throws409() {
    when(userRepository.findByEmail("john@example.com"))
      .thenReturn(Optional.of(user(UUID.randomUUID(), "john@example.com")));

    ResponseStatusException ex = catchThrowableOfType(
      () ->
        service.register(
          new AuthDtos.RegisterRequest("john@example.com", "password1", "John", "Doe")
        ),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  void login_validCredentials_returnsToken() {
    UUID id = UUID.randomUUID();
    User user = user(id, "john@example.com");
    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn("john@example.com");
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
    when(jwtTokenProvider.expirationSeconds()).thenReturn(3600L);

    AuthDtos.LoginResponse response = service.login(
      new AuthDtos.LoginRequest("john@example.com", "password1")
    );

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.expiresInSeconds()).isEqualTo(3600L);
  }

  @Test
  void logout_blankToken_throws400() {
    ResponseStatusException ex = catchThrowableOfType(
      () -> service.logout("   "),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void logout_validToken_addsToBlacklist() {
    when(jwtTokenProvider.blacklistTtlSeconds("jwt-token")).thenReturn(120L);

    service.logout("jwt-token");

    verify(jwtTokenProvider).validateToken("jwt-token");
    verify(blacklistService).addToBlacklist("jwt-token", 120L);
  }

  private static User user(UUID id, String email) {
    User user = User.builder().email(email).passwordHash("hash").role(Role.USER).active(true).build();
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
