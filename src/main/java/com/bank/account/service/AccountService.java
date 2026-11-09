package com.bank.account.service;

import com.bank.account.model.Account;
import com.bank.account.model.AccountTransaction;
import java.math.BigDecimal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<Account> createAccount(Account account);
    Mono<Account> getAccountByNumber(String accountNumber);
    Flux<Account> getAccountsByCustomerId(String customerId);
    Mono<AccountTransaction> executeTransaction(String accountNumber, String type, BigDecimal amount, String description);
}
