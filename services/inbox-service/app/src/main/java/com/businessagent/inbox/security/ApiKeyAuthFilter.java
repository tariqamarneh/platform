package com.businessagent.inbox.security;

import com.businessagent.inbox.client.AuthServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Missing X-API-Key header\"}");
            return;
        }

        var validation = authServiceClient.validateApiKey(apiKey);
        switch (validation.result()) {
            case VALID -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        validation.businessId(), null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("businessId", validation.businessId());
                filterChain.doFilter(request, response);
            }
            case INVALID -> {
                log.warn("Invalid API key presented");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid API key\"}");
            }
            case SERVICE_UNAVAILABLE -> {
                log.warn("Auth service unavailable during API key validation");
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Authentication service unavailable\"}");
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/health")
                || path.equals("/error")
                || path.startsWith("/actuator/")
                || path.startsWith("/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
