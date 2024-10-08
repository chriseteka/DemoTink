package com.chrisworks.transaction;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@UtilityClass
final class UserTransactionsPayloads {

    record TransactionsWrapper(List<Transaction> transactions) {

        record Transaction(String id, String accountId, String status, String providerMutability, Amount amount,
                           Descriptions descriptions, Dates dates, Identifiers identifiers, Types types) {

            record Descriptions(String original, String display) {}
            record Dates(LocalDate booked) {}
            record Identifiers(String providerTransactionId) {}
            record Types(String type) {}
        }
    }

    record AccountsAndBalancesWrapper(List<AccountsAndBalances> accounts) {

        record AccountsAndBalances(String id, String name, String type, String financialInstitutionId,
                                   String customerSegment, Dates dates, Identifiers identifiers, Balances balances) {

            record Balances(Booked booked, Available available) {

                record Booked(Amount amount) {}
                record Available(Amount amount) {}
            }

            record Identifiers(IBAN iban, FinancialInstitution financialInstitution) {

                record IBAN(String iban, String bban) {}
                record FinancialInstitution(String accountNumber, Map<String, Object> referenceNumbers) {}
            }

            record Dates(LocalDateTime lastRefreshed) {}
        }
    }

    record ProviderConsentsWrapper(List<ProviderConsent> providerConsents) {

        record ProviderConsent(String credentialsId, String providerName, String status, long sessionExpiryDate,
                               boolean sessionExtendable, List<String> accountIds, long statusUpdated) {}
    }

    //This object is shared by some of the wrappers
    record Amount(Value value, String currencyCode) {

        record Value(String unscaledValue, String scale) {}
    }
}
