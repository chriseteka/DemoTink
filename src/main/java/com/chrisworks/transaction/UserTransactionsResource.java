package com.chrisworks.transaction;

import com.chrisworks.transaction.UserTransactionsPayloads.AccountsAndBalancesWrapper;
import com.chrisworks.transaction.UserTransactionsPayloads.ProviderConsentsWrapper;
import com.chrisworks.transaction.UserTransactionsPayloads.TransactionsWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account/{userId}")
final class UserTransactionsResource {

    private final UserTransactionsService userTransactionsService;

    UserTransactionsResource(UserTransactionsService userTransactionsService) {
        this.userTransactionsService = userTransactionsService;
    }

    @GetMapping
    AccountsAndBalancesWrapper accountsAndBalances(@PathVariable(name = "userId") String userId) {
        return userTransactionsService.accountsAndBalancesByUserId(userId);
    }

    @GetMapping("/transactions")
    TransactionsWrapper accountsTransactions(@PathVariable(name = "userId") String userId) {
        return userTransactionsService.accountsTransactionsByUserId(userId);
    }

    @GetMapping("/provider-consents")
    ProviderConsentsWrapper providerConsents(@PathVariable(name = "userId") String userId) {
        return userTransactionsService.providerConsentsByUserId(userId);
    }
}
