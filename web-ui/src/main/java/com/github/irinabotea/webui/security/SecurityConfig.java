package com.github.irinabotea.webui.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  private final JwtCookieAuthenticationFilter jwtFilter;

  public SecurityConfig(JwtCookieAuthenticationFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
      .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            "/",
            "/login",
            "/register",
            "/css/**",
            "/js/**",
            "/webjars/**",
            "/error",
            "/favicon.ico"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable())
      .logout(l -> l.disable())
      .exceptionHandling(eh ->
        eh
          .accessDeniedPage("/error/access-denied")
          .authenticationEntryPoint((req, res, ex) -> {
            String target = req.getRequestURI();
            res.sendRedirect("/login?redirect=" + target);
          })
      )
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
