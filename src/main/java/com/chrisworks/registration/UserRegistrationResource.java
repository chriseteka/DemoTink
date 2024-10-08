package com.chrisworks.registration;

import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/users")
final class UserRegistrationResource {

    private final UserRegistrationService userRegistrationService;

    UserRegistrationResource(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/register-random")
    UserRegistrationPayloads.UserRegistrationResponse registerRandomUser() {
        return userRegistrationService.registerUser(UserRegistrationPayloads.UserRegistrationRequest.userInNL(UUID.randomUUID()));
    }

    @GetMapping("/registrations")
    Set<UserRegistrationPayloads.UserRegistrationResponse> registrations() {
        return userRegistrationService.userRegistrations();
    }

    @GetMapping("/registrations/{userId}")
    Optional<UserRegistrationPayloads.UserRegistrationResponse> registrationById(@PathVariable(name = "userId") String userId) {
        return userRegistrationService.userRegistrationById(userId);
    }

    @PatchMapping("/registrations/{userId}/register-external-account")
    URI registrationExternalAccount(@PathVariable(name = "userId") String userId) {
        return userRegistrationService.registerExternalAccount(userId);
    }
}
