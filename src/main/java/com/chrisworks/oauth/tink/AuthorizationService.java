package com.chrisworks.oauth.tink;

import com.chrisworks.AppEvents;
import com.chrisworks.user.UserDetails;
import com.chrisworks.configurations.AppConfigProperties;
import com.chrisworks.oauth.OAuthAuthorizationScope;
import com.chrisworks.oauth.tink.AuthorizationPayloads.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.function.Supplier;

import static com.chrisworks.oauth.tink.AuthorizationPayloads.USER_AUTHORIZATION_GRANT_SCOPE;

@Slf4j
@Component
final class AuthorizationService {

    private final TokenStore tokenStore;
    private final RestClient httpClient;
    private final AppConfigProperties.TinkConfig tinkConfig;

    AuthorizationService(TokenStore tokenStore, @Qualifier("tinkHttpClient") RestClient httpClient,
                                AppConfigProperties appConfigProperties) {
        this.tokenStore = tokenStore;
        this.httpClient = httpClient;
        this.tinkConfig = appConfigProperties.getTinkConfig();
    }

    @EventListener
    public <T extends OAuthAuthorizationScope> String authorizeClient(T eventWithScope) {

        if (tokenStore.clientAuthTokenDoesNotExistsOrIsExpired(eventWithScope)) {

            var formData = new LinkedMultiValueMap<String, String>();
            formData.add("scope", eventWithScope.scope());
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", tinkConfig.getClientId());
            formData.add("client_secret", tinkConfig.getClientSecret());

            var authorizationResponse = initiateAuthorization(formData, ClientAuthorizationResponse.class);

            tokenStore.storeClientAuthToken(eventWithScope, authorizationResponse);
        }

        return tokenStore.getClientAuthorizationToken(eventWithScope);
    }

    @EventListener
    public void userAuthorizationGrant(AppEvents.UserAuthorizationGrantEvent userAuthorizationGrantEvent) {

        final UserDetails userDetails = userAuthorizationGrantEvent.userDetails();
        final Supplier<AuthorizationGrantCode> authorizationGrantCodeSupplier =
                () -> {
                    final String clientAuthorizationAccessToken = authorizeClient(USER_AUTHORIZATION_GRANT_SCOPE);
                    final LinkedMultiValueMap<String, String> formData =
                            userAuthorizationGrantEvent.externalAccountOperation()
                                    .initFormDataWith(userDetails.name(), userDetails.id());

                    return httpClient
                            .post()
                            .uri(u -> u.path(userAuthorizationGrantEvent.externalAccountOperation().getRequestPath()).build())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .headers(h -> h.setBearerAuth(clientAuthorizationAccessToken))
                            .body(formData)
                            .retrieve()
                            .body(AuthorizationGrantCode.class);
                };

        switch (userAuthorizationGrantEvent.externalAccountOperation()) {
            case EXTERNAL_ACCOUNT_REGISTRATION              ->
                    prepareTINKURLForExternalAccountRegistration(userDetails, authorizationGrantCodeSupplier);
            case EXTERNAL_ACCOUNT_BALANCES_OR_TRANSACTIONS  ->
                    prepareUserAccessTokenForBalancesOrTransactionsRetrieval(userDetails.id(), authorizationGrantCodeSupplier);
        }
    }

    //We need to figure out how to manage the callback url
    private void prepareTINKURLForExternalAccountRegistration(
            UserDetails userDetails, Supplier<AuthorizationGrantCode> authorizationGrantCodeSupplier) {

        //Build URL: https://link.tink.com/1.0/transactions/connect-accounts?client_id={YOUR_CLIENT_ID}&state={OPTIONAL_STATE_CODE_THAT_YOU_SPECIFIED}&redirect_uri=https://console.tink.com/callback&authorization_code={USER_AUTHORIZATION_CODE}&market=GB&locale=en_US
        var url = "https://link.tink.com/1.0/transactions/connect-accounts?client_id=%s&redirect_uri=https://console.tink.com/callback&authorization_code=%s&market=%s&locale=%s"
                .formatted(tinkConfig.getClientId(), authorizationGrantCodeSupplier.get().value(), userDetails.market(), userDetails.locale());

        tokenStore.storeBankRegistrationURL(userDetails.id(), URI.create(url));
    }

    private void prepareUserAccessTokenForBalancesOrTransactionsRetrieval(
            String userId, Supplier<AuthorizationGrantCode> authorizationGrantCodeSupplier) {

        if (tokenStore.userAuthTokenDoesNotExistsOrIsExpired(userId)) {

            var formData = new LinkedMultiValueMap<String, String>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", tinkConfig.getClientId());
            formData.add("client_secret", tinkConfig.getClientSecret());
            formData.add("code", authorizationGrantCodeSupplier.get().value());

            var authorizationResponse = initiateAuthorization(formData, UserAuthorizationResponse.class);
            tokenStore.storeUserAuthToken(userId, authorizationResponse);
        }
    }

    private <T extends AuthorizationResponse> T initiateAuthorization(LinkedMultiValueMap<String, String> formData, Class<T> tClass) {

        return httpClient
                .post()
                .uri(u -> u.path(AppConfigProperties.TinkConfig.ApiPaths.TINK_OAUTH_TOKEN_INITIALIZATION_PATH).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(tClass);
    }
}
