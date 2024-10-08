package com.chrisworks.transaction;

import com.chrisworks.AppEvents;
import com.chrisworks.configurations.AppConfigProperties.TinkConfig;
import com.chrisworks.oauth.tink.TokenStore;
import com.chrisworks.transaction.UserTransactionsPayloads.AccountsAndBalancesWrapper;
import com.chrisworks.transaction.UserTransactionsPayloads.TransactionsWrapper;
import com.chrisworks.user.UserDetailsRetriever;
import com.chrisworks.transaction.UserTransactionsPayloads.ProviderConsentsWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
final class UserTransactionsService {

    private final TokenStore tokenStore;
    private final RestClient httpClient;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDetailsRetriever userDetailsRetriever;

    UserTransactionsService(TokenStore tokenStore, @Qualifier("tinkHttpClient") RestClient httpClient,
                            ApplicationEventPublisher eventPublisher, UserDetailsRetriever userDetailsRetriever) {
        this.tokenStore = tokenStore;
        this.httpClient = httpClient;
        this.eventPublisher = eventPublisher;
        this.userDetailsRetriever = userDetailsRetriever;
    }

    AccountsAndBalancesWrapper accountsAndBalancesByUserId(String userID) {
        return executeCallToTINKEndpointForUserData(userID,
                TinkConfig.ApiPaths.TINK_USER_ACCOUNTS_AND_BALANCES_RETRIEVAL_PATH, AccountsAndBalancesWrapper.class);
    }

    TransactionsWrapper accountsTransactionsByUserId(String userID) {
        return executeCallToTINKEndpointForUserData(userID,
                TinkConfig.ApiPaths.TINK_USER_ACCOUNTS_TRANSACTIONS_RETRIEVAL_PATH, TransactionsWrapper.class);
    }

    ProviderConsentsWrapper providerConsentsByUserId(String userID) {
        return executeCallToTINKEndpointForUserData(userID,
                TinkConfig.ApiPaths.TINK_USER_PROVIDER_CONSENTS_PATH, ProviderConsentsWrapper.class);
    }

    private <T> T executeCallToTINKEndpointForUserData(String userID, String endpointPath, Class<T> tClass) {

        //Emit user auth event for accounts-balances-or-transaction-retrieval purpose
        eventPublisher.publishEvent(
                AppEvents.UserAuthorizationGrantEvent.initForAccountAndBalancesOrTransactionFetchEvent(
                        userDetailsRetriever.fetchUserRegistrationByUserId(userID))
        );

        //Extract the user auth token
        var userAuthorizationToken = tokenStore.getUserAuthorizationToken(userID);

        //Initiate the downstream call with the token
        return httpClient
                .get()
                .uri(u -> u.path(endpointPath).build())
                .headers(h -> h.setBearerAuth(userAuthorizationToken))
                .retrieve()
                .body(tClass);

    }
}
