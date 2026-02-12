package com.marketplace.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.infrastructure.rest.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
                .requestMatchers("/api/listings/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.POST, "/api/listings").hasRole("SELLER")
                .requestMatchers("/api/certification/**").hasRole("CONTROLLER")
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/pay").hasRole("CONTROLLER")
                .requestMatchers(HttpMethod.POST, "/api/payments/webhooks").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/waitlist/subscriptions").hasRole("BUYER")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    writeError(response, ErrorCode.AUTH_REQUIRED))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeError(response, ErrorCode.ACCESS_DENIED))
            )
            .httpBasic(Customizer.withDefaults());
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
