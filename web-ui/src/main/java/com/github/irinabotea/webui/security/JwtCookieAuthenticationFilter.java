package com.github.irinabotea.webui.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the JWT from the auth cookie on each request, validates it, and
 * populates the SecurityContext with a {@link UsernamePasswordAuthenticationToken}.
 */
@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtCookieService cookies;

    public JwtCookieAuthenticationFilter(JwtCookieService cookies) {
        this.cookies = cookies;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cookies.readToken(request);
            if (token != null) {
                Claims claims = cookies.parse(token);
                if (claims != null) {
                    String email = claims.getSubject();
                    Object roleClaim = claims.get("role");
                    String role = roleClaim == null ? "USER" : roleClaim.toString();
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        email,
                        token,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
