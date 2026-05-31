package com.github.narcispurghel.userservice.service;

import com.github.narcispurghel.userservice.dto.AuthDtos;
import com.github.narcispurghel.userservice.entity.AdopterProfile;
import com.github.narcispurghel.userservice.entity.Role;
import com.github.narcispurghel.userservice.entity.User;
import com.github.narcispurghel.userservice.entity.valueobject.Name;
import com.github.narcispurghel.userservice.repository.UserJpaRepository;
import com.github.narcispurghel.userservice.security.JwtTokenProvider;
import com.github.narcispurghel.userservice.security.RedisTokenBlacklistService;
import java.util.Locale;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserJpaRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTokenBlacklistService blacklistService;

  public AuthService(
    UserJpaRepository userRepository,
    PasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager,
    JwtTokenProvider jwtTokenProvider,
    RedisTokenBlacklistService blacklistService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
    this.blacklistService = blacklistService;
  }

  @Transactional
  public AuthDtos.RegisterResponse register(AuthDtos.RegisterRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }

    User user = User.builder()
      .email(request.email().strip().toLowerCase(Locale.ROOT))
      .passwordHash(Objects.requireNonNull(passwordEncoder.encode(request.password())))
      .role(Role.USER)
      .active(true)
      .build();

    AdopterProfile profile = AdopterProfile.builder()
      .user(user)
      .firstName(Name.from(request.firstName()))
      .lastName(Name.from(request.lastName()))
      .animalExperience(UserMapper.toExperience(0))
      .build();
    user.setProfile(profile);

    try {
      user = userRepository.save(user);
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }
    return new AuthDtos.RegisterResponse(user.getId().toString(), user.getEmail());
  }

  @Transactional(readOnly = true)
  public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );
    User user = userRepository
      .findByEmail(authentication.getName())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
      );
    String token = jwtTokenProvider.generateToken(user);
    return new AuthDtos.LoginResponse(token, jwtTokenProvider.expirationSeconds());
  }

  public void logout(String bearerToken) {
    if (bearerToken == null || bearerToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing bearer token");
    }
    jwtTokenProvider.validateToken(bearerToken);
    blacklistService.addToBlacklist(bearerToken, jwtTokenProvider.blacklistTtlSeconds(bearerToken));
  }
}
