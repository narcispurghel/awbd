package com.github.irinabotea.webui.security;

import com.github.irinabotea.webui.client.BackendException;
import com.github.irinabotea.webui.client.UserServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/** Blacklists the current JWT on the backend when Spring Security logs the user out. */
@Component
public class BackendLogoutHandler implements LogoutHandler {

  private static final Logger log = LoggerFactory.getLogger(BackendLogoutHandler.class);

  private final UserServiceClient client;

  public BackendLogoutHandler(UserServiceClient client) {
    this.client = client;
  }

  @Override
  public void logout(
    HttpServletRequest request,
    HttpServletResponse response,
    @Nullable Authentication authentication
  ) {
    try {
      client.logout();
    } catch (BackendException ex) {
      log.warn("Backend logout failed (status {}): {}", ex.status(), ex.getMessage());
    }
  }
}
