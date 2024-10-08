package com.chrisworks.oauth.tink;

import com.chrisworks.oauth.OAuthAuthorizationScope;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
final class AuthorizationPayloads {

    final static OAuthAuthorizationScope USER_AUTHORIZATION_GRANT_SCOPE = () -> "authorization:grant";

    interface AuthorizationResponse {
        String accessToken();
        String tokenType();
        String scope();
        LocalDateTime expiresAt();

        default boolean isExpired() {
            return expiresAt().isBefore(LocalDateTime.now());
        }
    }

    record ClientAuthorizationResponse(
            String accessToken, String tokenType, String scope, LocalDateTime expiresAt) implements AuthorizationResponse {

        @JsonCreator
        ClientAuthorizationResponse(
                @JsonProperty("access_token") String accessToken, @JsonProperty("token_type") String tokenType,
                @JsonProperty("expires_in") int expiresIn, String scope) {
            this(accessToken, tokenType, scope, LocalDateTime.now().plusSeconds(expiresIn));
        }
    }

    //How to use the refresh token
    record UserAuthorizationResponse(
            String accessToken, String refreshToken, String tokenType, String scope, LocalDateTime expiresAt) implements AuthorizationResponse {

        @JsonCreator
        UserAuthorizationResponse(
                @JsonProperty("access_token") String accessToken, @JsonProperty("token_type") String tokenType,
                @JsonProperty("expires_in") int expiresIn, @JsonProperty("refresh_token") String refreshToken, String scope) {
            this(accessToken, refreshToken, tokenType, scope, LocalDateTime.now().plusSeconds(expiresIn));
        }
    }

    record AuthorizationGrantCode(@JsonProperty("code") String value) {}
}
