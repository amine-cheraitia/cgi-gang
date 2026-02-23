package com.marketplace.shared.infrastructure.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dev")
public class DevController {

    @GetMapping("/seeds")
    public Map<String, Object> seeds() {
        return Map.of(
            "users", List.of(
                Map.of("username", "seller",     "password", "seller123",     "role", "SELLER"),
                Map.of("username", "buyer",      "password", "buyer123",      "role", "BUYER"),
                Map.of("username", "controller", "password", "controller123", "role", "CONTROLLER"),
                Map.of("username", "admin",      "password", "admin123",      "role", "ADMIN + SELLER + BUYER + CONTROLLER")
            ),
            "listings", List.of(
                Map.of("id", "lst_seed_001", "seller", "seller", "event", "evt_taylor_paris", "price", "80.00 EUR", "status", "CERTIFIED"),
                Map.of("id", "lst_seed_002", "seller", "seller2", "event", "evt_taylor_paris", "price", "120.00 EUR", "status", "PENDING_CERTIFICATION")
            )
        );
    }
}
