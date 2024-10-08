package com.chrisworks.configurations;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;

@Data
@Configuration
@ConfigurationProperties(prefix = "app-config")
public class AppConfigProperties {

    private TinkConfig tinkConfig;
    private String fsDBStoreFullPath;

    @Data
    public static final class TinkConfig {

        private String baseUrl;
        private String clientId;
        private String clientSecret;

        public enum ExternalAccountOperation {

            EXTERNAL_ACCOUNT_REGISTRATION(
                    ApiPaths.EXTERNAL_ACCOUNT_REGISTRATION_OAUTH_GRANT_PATH,
                    "authorization:read,authorization:grant,credentials:refresh,credentials:read,credentials:write,providers:read,user:read"
            ),

            EXTERNAL_ACCOUNT_BALANCES_OR_TRANSACTIONS(
                    ApiPaths.EXTERNAL_ACCOUNT_BALANCES_OR_TRANSACTIONS_OAUTH_GRANT_PATH,
                    "accounts:read,balances:read,transactions:read,provider-consents:read"
            );

            @Getter
            final String requestPath;
            final String scopes;

            ExternalAccountOperation(String requestPath, String scopes) {
                this.requestPath = requestPath;
                this.scopes = scopes;
            }

            public LinkedMultiValueMap<String, String> initFormDataWith(String maybeUserName, String externalUserId) {

                var formData = new LinkedMultiValueMap<String, String>();
                formData.add("scope", scopes);
                formData.add("id_hint", maybeUserName);
                formData.add("external_user_id", externalUserId);

                return switch (this) {
                    case EXTERNAL_ACCOUNT_REGISTRATION -> {
                        formData.add("actor_client_id", "df05e4b379934cd09963197cc855bfe9");
                        yield formData;
                    }
                    case EXTERNAL_ACCOUNT_BALANCES_OR_TRANSACTIONS -> formData;
                };
            }
        }

        @UtilityClass
        public static final class ApiPaths {

            static final String EXTERNAL_ACCOUNT_REGISTRATION_OAUTH_GRANT_PATH = "/api/v1/oauth/authorization-grant/delegate";
            static final String EXTERNAL_ACCOUNT_BALANCES_OR_TRANSACTIONS_OAUTH_GRANT_PATH = "/api/v1/oauth/authorization-grant";

            public static final String TINK_OAUTH_TOKEN_INITIALIZATION_PATH = "/api/v1/oauth/token";
            public static final String TINK_USER_REGISTRATION_PATH = "/api/v1/user/create";
            public static final String TINK_USER_ACCOUNTS_AND_BALANCES_RETRIEVAL_PATH = "/data/v2/accounts";
            public static final String TINK_USER_ACCOUNTS_TRANSACTIONS_RETRIEVAL_PATH = "/data/v2/transactions";
            public static final String TINK_USER_PROVIDER_CONSENTS_PATH = "/api/v1/provider-consents";
        }
    }
}
