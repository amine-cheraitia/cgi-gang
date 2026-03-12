package com.marketplace.user.infrastructure.rest;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.user.infrastructure.persistence.SpringDataUserRepository;
import com.marketplace.user.infrastructure.persistence.UserEntity;
import com.marketplace.user.infrastructure.rest.dto.JwtResponse;
import com.marketplace.user.infrastructure.rest.dto.LoginRequest;
import com.marketplace.user.infrastructure.rest.dto.RegisterRequest;
import com.marketplace.user.infrastructure.rest.dto.UserProfileResponse;
import com.marketplace.security.JwtService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Inscription et authentification")
public class AuthController {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(SpringDataUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un compte", description = "Inscrit un nouvel utilisateur 'client' (achat / vente).")
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest request) {
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
        // Tous les comptes créés via l'API publique sont des clients
        user.setRole("CLIENT");
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
        return new UserProfileResponse(user.getUsername(), user.getEmail(), user.getRole());
    }

    @PostMapping("/login")
    @Operation(summary = "Authentification client", description = "Authentifie un utilisateur 'client' (espace public) et renvoie un token JWT.")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                List.of(user.getRole().split(","))
        );
        return JwtResponse.bearer(token);
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Authentification admin", description = "Authentifie un utilisateur avec role CONTROLLER ou ADMIN pour l'espace d'administration.")
    public JwtResponse adminLogin(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
        }

        String roles = user.getRole();
        boolean isAdminLike = roles.contains("ADMIN") || roles.contains("CONTROLLER");
        if (!isAdminLike) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                List.of(user.getRole().split(","))
        );
        return JwtResponse.bearer(token);
    }

    @GetMapping("/me")
    @Operation(summary = "Profil connecté", description = "Retourne les informations de l'utilisateur authentifié (JWT Bearer).")
    public UserProfileResponse me(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserProfileResponse(user.getUsername(), user.getEmail(), user.getRole());
    }
}
