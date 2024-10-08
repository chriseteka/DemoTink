package com.chrisworks.registration;

import com.chrisworks.AppEvents;
import com.chrisworks.user.UserDetails;
import com.chrisworks.configurations.AppConfigProperties;
import com.chrisworks.oauth.tink.TokenStore;
import com.chrisworks.user.UserDetailsRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static com.chrisworks.registration.UserRegistrationPayloads.*;

@Component
final class UserRegistrationService implements UserDetailsRetriever {

    private final String fsDBPath;
    private final TokenStore tokenStore;
    private final RestClient httpClient;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<UserRegistrationResponse, UserRegistrationRequest> userRegistrations;

    UserRegistrationService(TokenStore tokenStore, @Qualifier("tinkHttpClient") RestClient httpClient,
                            ApplicationEventPublisher eventPublisher, AppConfigProperties appConfigProperties) throws IOException {
        this.tokenStore = tokenStore;
        this.httpClient = httpClient;
        this.eventPublisher = eventPublisher;
        this.fsDBPath = appConfigProperties.getFsDBStoreFullPath();
        this.userRegistrations = initData(appConfigProperties.getFsDBStoreFullPath());
    }

    UserRegistrationResponse registerUser(UserRegistrationRequest registrationRequest) {

        //Emit client auth event
        eventPublisher.publishEvent(USER_REGISTRATION_AUTHORIZATION_SCOPE);

        //Get Access Token
        var authorizationToken = tokenStore.getClientAuthorizationToken(USER_REGISTRATION_AUTHORIZATION_SCOPE);

        //Call Downstream
        var userRegistrationResponse = httpClient
                .post()
                .uri(u -> u.path(AppConfigProperties.TinkConfig.ApiPaths.TINK_USER_REGISTRATION_PATH).build())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(authorizationToken))
                .body(registrationRequest)
                .retrieve()
                .body(UserRegistrationResponse.class);

        //Add user to registration list
        addRegistration(userRegistrationResponse, registrationRequest);

        //Return
        return userRegistrationResponse;
    }

    Set<UserRegistrationResponse> userRegistrations() {
        return userRegistrations.keySet();
    }

    Optional<UserRegistrationResponse> userRegistrationById(String userId) {
        return userRegistrations.keySet().stream().filter(u -> u.userId().equalsIgnoreCase(userId)).findFirst();
    }

    public UserDetails fetchUserRegistrationByUserId(String userId) {
        var userRegistration =  userRegistrations
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().userId().equalsIgnoreCase(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not registered"))
                .getValue();

        return new UserDetails(userId, "Naive User", userRegistration.market(), userRegistration.locale());
    }

    URI registerExternalAccount(String userID) {

        //Emit user auth event for external-account-registration purpose
        eventPublisher.publishEvent(AppEvents.UserAuthorizationGrantEvent
                .initForAccountRegistrationEvent(fetchUserRegistrationByUserId(userID)));

        return tokenStore.getBankRegistrationURL(userID);
    }

    private void addRegistration(UserRegistrationResponse userRegistrationResponse, UserRegistrationRequest registrationRequest) {
        userRegistrations.put(userRegistrationResponse, registrationRequest);
        try {
            var output = new BufferedWriter(new FileWriter(fsDBPath, true));
            output.write("\n%s, %s".formatted(userRegistrationResponse.toCSVLine(), registrationRequest.toCSVLine()));
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<UserRegistrationResponse, UserRegistrationRequest> initData(String resourcePath) throws IOException {
        final Map<UserRegistrationResponse, UserRegistrationRequest> oldRecords = new HashMap<>();

        try (Stream<String> lines = Files.lines(Paths.get(resourcePath))) {
            lines.skip(1)
                    .filter(StringUtils::hasText)
                    .forEach(line -> {
                        var lineArr = line.split(",");
                        var res = UserRegistrationResponse.fromCSVLine(lineArr);
                        var req = UserRegistrationRequest.fromCSVLine(lineArr);

                        oldRecords.put(res, req);
                    });
        }

        return oldRecords;
    }
}
