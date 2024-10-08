package com.chrisworks;

import com.chrisworks.configurations.AppConfigProperties.TinkConfig;
import com.chrisworks.user.UserDetails;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class AppEvents {

    public record UserAuthorizationGrantEvent(UserDetails userDetails, TinkConfig.ExternalAccountOperation externalAccountOperation) {

        public static UserAuthorizationGrantEvent initForAccountRegistrationEvent(UserDetails userDetails) {
            return new UserAuthorizationGrantEvent(userDetails, TinkConfig.ExternalAccountOperation.EXTERNAL_ACCOUNT_REGISTRATION);
        }

        public static UserAuthorizationGrantEvent initForAccountAndBalancesOrTransactionFetchEvent(UserDetails userDetails) {
            return new UserAuthorizationGrantEvent(userDetails, TinkConfig.ExternalAccountOperation.EXTERNAL_ACCOUNT_BALANCES_OR_TRANSACTIONS);
        }
    }
}
