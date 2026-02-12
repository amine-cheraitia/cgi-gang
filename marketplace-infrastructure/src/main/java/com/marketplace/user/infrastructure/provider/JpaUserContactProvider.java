package com.marketplace.user.infrastructure.provider;

import com.marketplace.notification.application.port.UserContactProvider;
import com.marketplace.user.infrastructure.persistence.SpringDataUserRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaUserContactProvider implements UserContactProvider {
    private final SpringDataUserRepository repository;

    public JpaUserContactProvider(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserContact getByUserId(String userId) {
        return repository.findById(userId)
            .map(user -> new UserContact(user.getId(), user.getUsername(), user.getEmail()))
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
