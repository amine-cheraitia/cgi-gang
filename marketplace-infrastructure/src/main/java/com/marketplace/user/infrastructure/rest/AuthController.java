package com.marketplace.user.infrastructure.rest;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.user.infrastructure.persistence.SpringDataUserRepository;
import com.marketplace.user.infrastructure.persistence.UserEntity;
import com.marketplace.user.infrastructure.rest.dto.AuthResponse;
import com.marketplace.user.infrastructure.rest.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Inscription et authentification")
public class AuthController {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(SpringDataUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un compte", description = "Inscrit un nouvel utilisateur avec le role SELLER ou BUYER.")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(request.role().toUpperCase());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @GetMapping("/me")
    @Operation(summary = "Profil connecté", description = "Retourne les informations de l'utilisateur authentifié (valide les credentials HTTP Basic).")
    public AuthResponse me(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
