package com.github.irinabotea.webui.security;

import com.github.irinabotea.webui.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/** Reads/writes the JWT cookie used for authentication and parses tokens. */
@Service
public class JwtCookieService {

    private final AppProperties properties;
    private final SecretKey signingKey;

    public JwtCookieService(AppProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public void writeCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        Cookie cookie = new Cookie(properties.jwt().cookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setSecure(properties.jwt().cookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge((int) Math.max(0L, maxAgeSeconds));
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(properties.jwt().cookieName(), "");
        cookie.setHttpOnly(true);
        cookie.setSecure(properties.jwt().cookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public @Nullable String readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String name = properties.jwt().cookieName();
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                String value = c.getValue();
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    public @Nullable Claims parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return jws.getPayload();
        } catch (Exception ex) {
            return null;
        }
    }
}
