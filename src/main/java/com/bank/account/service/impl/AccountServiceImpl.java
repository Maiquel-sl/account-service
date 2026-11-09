package com.bank.account.service.impl;

import com.bank.account.model.Account;
import com.bank.account.model.AccountTransaction;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.AccountTransactionRepository;
import com.bank.account.service.AccountService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final AccountTransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              AccountTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Mono<Account> createAccount(Account account) {
        // Validación de Reglas de Negocio usando Pattern Matching de Java 27
        if ("ENTERPRISE".equals(account.getCustomerType()) && 
            ("SAVINGS".equals(account.getType()) || "FIXED_TERM".equals(account.getType()))) {
            return Mono.error(new IllegalArgumentException("Un cliente empresarial no puede tener una cuenta de ahorro o de plazo fijo"));
        }

        return accountRepository.findByCustomerId(account.getCustomerId())
                .collectList()
                .flatMap(existingAccounts -> {
                    // Validar límite para cliente Personal
                    if ("PERSONAL".equals(account.getCustomerType())) {
                        boolean alreadyHasType = existingAccounts.stream()
                                .anyMatch(acc -> acc.getType().equals(account.getType()));
                        if (alreadyHasType) {
                            return Mono.error(new IllegalArgumentException("Cliente personal ya tiene una cuenta de tipo: " + account.getType()));
                        }
                    }
                    
                    account.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    account.setStatus("ACTIVE");
                    account.setCreatedAt(LocalDateTime.now());
                    account.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(account);
                });
    }

    @Override
    public Mono<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Flux<Account> getAccountsByCustomerId(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    public Mono<AccountTransaction> executeTransaction(String accountNumber, String type, BigDecimal amount, String description) {
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(new RuntimeException("Cuenta no encontrada")))
                .flatMap(account -> {
                    // Actualizar saldo según la operación bancaria
                    BigDecimal newBalance = switch (type) {
                        case "DEPOSIT" -> account.getBalance().add(amount);
                        case "WITHDRAWAL" -> {
                            if (account.getBalance().compareTo(amount) < 0) {
                                throw new IllegalArgumentException("Saldo insuficiente para efectuar el retiro");
                            }
                            yield account.getBalance().subtract(amount);
                        }
                        default -> throw new IllegalArgumentException("Tipo de operación inválida");
                    };
                    
                    account.setBalance(newBalance);
                    account.setUpdatedAt(LocalDateTime.now());
                    
                    AccountTransaction tx = new AccountTransaction();
                    tx.setTransactionNumber("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
                    tx.setAccountId(account.getId());
                    tx.setAccountNumber(account.getAccountNumber());
                    tx.setType(type);
                    tx.setAmount(amount);
                    tx.setDescription(description);
                    tx.setTimestamp(LocalDateTime.now());

                    return accountRepository.save(account)
                            .then(transactionRepository.save(tx));
                });
    }
}
