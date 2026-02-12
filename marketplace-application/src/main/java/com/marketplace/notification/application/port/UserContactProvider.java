package com.marketplace.notification.application.port;

public interface UserContactProvider {
    UserContact getByUserId(String userId);

    record UserContact(String userId, String username, String email) {}
}
