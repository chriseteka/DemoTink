package com.chrisworks.oauth.tink;

import com.chrisworks.oauth.OAuthAuthorizationScope;
import com.chrisworks.oauth.tink.AuthorizationPayloads.ClientAuthorizationResponse;
import com.chrisworks.oauth.tink.AuthorizationPayloads.UserAuthorizationResponse;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//This should be implemented in form of a cache with a TTL
@Component
public final class TokenStore {

    private final Map<String, URI> userToTinkBankRegistrationURL = new ConcurrentHashMap<>();
    private final Map<String, UserAuthorizationResponse> usersAuthorizationTokens = new ConcurrentHashMap<>();
    private final Map<OAuthAuthorizationScope, ClientAuthorizationResponse> clientAuthorizationTokens = new ConcurrentHashMap<>();

    <T extends OAuthAuthorizationScope> void storeClientAuthToken(T authorizationScope, ClientAuthorizationResponse authorizationResponse) {
        clientAuthorizationTokens.put(authorizationScope, authorizationResponse);
    }

    <T extends OAuthAuthorizationScope> boolean clientAuthTokenDoesNotExistsOrIsExpired(T authorizationScope) {
        return Optional.ofNullable(clientAuthorizationTokens.get(authorizationScope))
                .map(ClientAuthorizationResponse::isExpired)
                .orElse(true);
    }

    void storeUserAuthToken(String userId, UserAuthorizationResponse authorizationResponse) {
        usersAuthorizationTokens.put(userId, authorizationResponse);
    }

    boolean userAuthTokenDoesNotExistsOrIsExpired(String userId) {
        return Optional.ofNullable(usersAuthorizationTokens.get(userId))
                .map(UserAuthorizationResponse::isExpired)
                .orElse(true);
    }

    void storeBankRegistrationURL(String userId, URI url) {
        userToTinkBankRegistrationURL.put(userId, url);
    }

    public <T extends OAuthAuthorizationScope> String getClientAuthorizationToken(T authorizationScope) {
        return Optional.ofNullable(clientAuthorizationTokens.get(authorizationScope))
                .map(ClientAuthorizationResponse::accessToken)
                .orElseThrow(() -> new RuntimeException("Client Authorization Token not found"));
    }

    public String getUserAuthorizationToken(String userId) {
        return Optional.ofNullable(usersAuthorizationTokens.get(userId))
                .map(UserAuthorizationResponse::accessToken)
                .orElseThrow(() -> new RuntimeException("User Authorization Token not found"));
    }

    public URI getBankRegistrationURL(String userId) {
        return Optional.ofNullable(userToTinkBankRegistrationURL.remove(userId))
                .orElseThrow(() -> new RuntimeException("Bank registration URL not found"));
    }
}
