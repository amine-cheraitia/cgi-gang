package com.marketplace.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.infrastructure.rest.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/listings").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                .requestMatchers("/api/listings/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.POST, "/api/listings").hasRole("SELLER")
                .requestMatchers("/api/certification/**").hasRole("CONTROLLER")
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/pay").hasRole("CONTROLLER")
                .requestMatchers(HttpMethod.POST, "/api/payments/webhooks").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/waitlist/subscriptions").hasRole("BUYER")
                .requestMatchers(HttpMethod.DELETE, "/api/waitlist/subscriptions").hasRole("BUYER")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeError(response, ErrorCode.ACCESS_DENIED))
            )
            .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint((request, response, authException) -> {
                if (authException instanceof BadCredentialsException) {
                    writeError(response, ErrorCode.AUTH_BAD_CREDENTIALS);
                    return;
                }
                writeError(response, ErrorCode.AUTH_REQUIRED);
            }));
        return http.build();
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService() {
        UserDetails seller = User.withUsername("seller")
            .password("{noop}seller123")
            .roles("SELLER")
            .build();
        UserDetails controller = User.withUsername("controller")
            .password("{noop}controller123")
            .roles("CONTROLLER")
            .build();
        UserDetails buyer = User.withUsername("buyer")
            .password("{noop}buyer123")
            .roles("BUYER")
            .build();
        UserDetails admin = User.withUsername("admin")
            .password("{noop}admin123")
            .roles("ADMIN", "CONTROLLER", "SELLER", "BUYER")
            .build();
        return new InMemoryUserDetailsManager(seller, controller, buyer, admin);
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse payload = ErrorResponse.of(code.code(), code.defaultMessage());
        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }
}
