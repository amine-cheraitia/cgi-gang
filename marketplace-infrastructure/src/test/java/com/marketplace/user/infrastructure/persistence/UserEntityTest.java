package com.marketplace.user.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void shouldExposeGettersAndSetters() {
        UserEntity entity = new UserEntity();
        entity.setId("usr-1");
        entity.setUsername("buyer");
        entity.setRole("BUYER");
        entity.setEmail("buyer@marketplace.local");

        assertThat(entity.getId()).isEqualTo("usr-1");
        assertThat(entity.getUsername()).isEqualTo("buyer");
        assertThat(entity.getRole()).isEqualTo("BUYER");
        assertThat(entity.getEmail()).isEqualTo("buyer@marketplace.local");
    }
}
