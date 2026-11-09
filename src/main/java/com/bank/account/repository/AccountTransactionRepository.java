package com.bank.account.repository;

import com.bank.account.model.AccountTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AccountTransactionRepository extends ReactiveMongoRepository<AccountTransaction, String> {
    Flux<AccountTransaction> findByAccountId(String accountId);
}
