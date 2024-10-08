package com.chrisworks.registration;

import com.chrisworks.oauth.OAuthAuthorizationScope;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
final class UserRegistrationPayloads {

    static final OAuthAuthorizationScope USER_REGISTRATION_AUTHORIZATION_SCOPE = () -> "user:create";

    public record UserRegistrationRequest(@JsonProperty("external_user_id") String id, String market, String locale) {

        static UserRegistrationRequest userInNL(UUID userId) {
            return new UserRegistrationRequest(userId.toString(), "NL", "en_US");
        }

        static UserRegistrationRequest fromCSVLine(String... lineArr) {
            return new UserRegistrationRequest(lineArr[0].trim(), lineArr[2].trim(), lineArr[3].trim());
        }

        public String toCSVLine() {
            return "%s, %s".formatted(market, locale);
        }
    }

    record UserRegistrationResponse(
            @JsonProperty("external_user_id") String userId, @JsonProperty("user_id") String registrationId) {

        static UserRegistrationResponse fromCSVLine(String... lineArr) {
            return new UserRegistrationResponse(lineArr[0].trim(), lineArr[1].trim());
        }

        String toCSVLine() {
            return "%s, %s".formatted(userId, registrationId);
        }
    }

}
