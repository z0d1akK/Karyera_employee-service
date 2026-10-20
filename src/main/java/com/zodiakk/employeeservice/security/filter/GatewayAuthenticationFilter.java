package com.zodiakk.employeeservice.security.filter;

import com.zodiakk.employeeservice.common.constants.Headers;
import com.zodiakk.employeeservice.common.constants.SecurityConstants;
import com.zodiakk.employeeservice.security.principal.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userIdHeader = request.getHeader(Headers.USER_ID);
        String roleHeader = request.getHeader(Headers.USER_ROLE);
        String emailHeader = request.getHeader(Headers.USER_EMAIL);

        if (userIdHeader != null && roleHeader != null && emailHeader != null) {
            try {
                String authority = toAuthority(roleHeader);

                CurrentUser currentUser = CurrentUser.builder()
                        .userId(UUID.fromString(userIdHeader))
                        .role(authority)
                        .email(emailHeader)
                        .build();

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                currentUser,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            } catch (IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String toAuthority(String role) {
        return role.startsWith(SecurityConstants.ROLE_PREFIX)
                ? role
                : SecurityConstants.ROLE_PREFIX + role;
    }
}
