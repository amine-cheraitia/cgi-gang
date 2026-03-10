package com.marketplace.user.infrastructure.service;

import com.marketplace.user.infrastructure.persistence.SpringDataUserRepository;
import com.marketplace.user.infrastructure.persistence.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final SpringDataUserRepository userRepository;

    public CurrentUserService(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}

