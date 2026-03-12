package com.marketplace.user.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void shouldExposeGettersAndSetters() {
        UserEntity entity = new UserEntity();
        entity.setId("usr-1");
        entity.setUsername("client");
        entity.setRole("CLIENT");
        entity.setEmail("client@marketplace.local");

        assertThat(entity.getId()).isEqualTo("usr-1");
        assertThat(entity.getUsername()).isEqualTo("client");
        assertThat(entity.getRole()).isEqualTo("CLIENT");
        assertThat(entity.getEmail()).isEqualTo("client@marketplace.local");
    }
}
