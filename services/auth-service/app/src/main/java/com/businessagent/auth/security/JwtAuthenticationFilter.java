package com.businessagent.auth.security;

import com.businessagent.auth.model.enums.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            jwtProvider.parseAccessToken(token).ifPresent(claims -> {
                UUID userId = UUID.fromString(claims.getSubject());
                String businessIdStr = claims.get("businessId", String.class);
                UUID businessId = businessIdStr != null ? UUID.fromString(businessIdStr) : null;
                String email = claims.get("email", String.class);
                Role role = Role.valueOf(claims.get("role", String.class));

                AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, businessId, email, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser, null, authenticatedUser.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: userId={}, businessId={}, role={}",
                        userId, businessId, role);
            });
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/")
                || path.equals("/api/v1/admin/login")
                || path.equals("/api/v1/keys/verify")
                || path.equals("/health")
                || path.startsWith("/actuator/")
                || path.startsWith("/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
