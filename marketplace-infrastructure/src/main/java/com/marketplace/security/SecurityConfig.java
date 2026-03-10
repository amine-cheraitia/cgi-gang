package com.marketplace.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.infrastructure.rest.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(ObjectMapper objectMapper, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.objectMapper = objectMapper;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/admin/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/listings").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/dev/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                // Tout utilisateur authentifié peut créer/vendre/acheter
                .requestMatchers("/api/listings/**").authenticated()
                .requestMatchers("/api/certification/**").hasRole("CONTROLLER")
                .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/orders/*/pay").hasRole("CONTROLLER")
                .requestMatchers(HttpMethod.POST, "/api/payments/webhooks").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/waitlist/subscriptions").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/waitlist/subscriptions").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeError(response, ErrorCode.ACCESS_DENIED))
            )
            .httpBasic(httpBasic -> httpBasic.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.httpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse payload = ErrorResponse.of(code.code(), code.defaultMessage());
        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }
}
