package com.bank.account.service.impl;

import com.bank.account.events.AccountTransactionEvent;
import com.bank.account.messaging.AccountEventPublisher;
import com.bank.account.model.Account;
import com.bank.account.model.AccountTransaction;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.AccountTransactionRepository;
import com.bank.account.repository.CustomerReplicaRepository;
import com.bank.account.service.AccountService;
import com.bank.account.util.ReactiveFallbackHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final CustomerReplicaRepository customerReplicaRepository;
    private final AccountEventPublisher eventPublisher;

    public AccountServiceImpl(AccountRepository accountRepository,
                              AccountTransactionRepository transactionRepository,
                              CustomerReplicaRepository customerReplicaRepository,
                              AccountEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerReplicaRepository = customerReplicaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<Account> createAccount(Account account) {
        // 1. Validación local contra la réplica sincronizada por Kafka (Cero REST)
        return customerReplicaRepository.findById(account.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado en la réplica local")))
                .flatMap(customer -> {
                    if (!"ACTIVE".equals(customer.getStatus())) {
                        return Mono.error(new IllegalArgumentException("El cliente se encuentra inactivo"));
                    }

                    // Asignar el tipo confiable proveniente del evento
                    String verifiedType = customer.getType();
                    account.setCustomerType(verifiedType);

                    // 2. Regla empresarial: No puede tener Ahorro ni Plazo Fijo
                    if ("ENTERPRISE".equals(verifiedType) &&
                            ("SAVINGS".equals(account.getType()) || "FIXED_TERM".equals(account.getType()))) {
                        return Mono.error(new IllegalArgumentException(
                                "Un cliente empresarial no puede tener cuentas de ahorro o plazo fijo"));
                    }

                    // 3. Validar cuentas existentes del cliente
                    return accountRepository.findByCustomerId(account.getCustomerId())
                            .collectList()
                            .flatMap(existingAccounts -> {
                                if ("PERSONAL".equals(verifiedType)) {
                                    boolean alreadyHasType = existingAccounts.stream()
                                            .anyMatch(acc -> acc.getType().equals(account.getType()));
                                    if (alreadyHasType) {
                                        return Mono.error(new IllegalArgumentException(
                                                "Un cliente personal solo puede tener una cuenta de tipo: " + account.getType()));
                                    }
                                }

                                account.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                                account.setStatus("ACTIVE");
                                account.setCreatedAt(LocalDateTime.now());
                                account.setUpdatedAt(LocalDateTime.now());
                                return accountRepository.save(account);
                            });
                });
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericFluxFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Flux<Account> getAccountsByCustomerId(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
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
                    .then(transactionRepository.save(tx))
                    .doOnSuccess(savedTx -> eventPublisher.publish(new AccountTransactionEvent(
                        savedTx.getTransactionNumber(),
                        account.getCustomerId(),
                        account.getId(),
                        account.getType(),
                        account.getAccountNumber(),
                        savedTx.getType(),
                        savedTx.getAmount(),
                        account.getCurrency(),
                        savedTx.getDescription(),
                        savedTx.getTimestamp())));
                });
    }

    // =========================================================================
    // MÉTODOS FALLBACK GENÉRICOS PARA TODA LA CLASE
    // =========================================================================

    @SuppressWarnings("unused")
    private <T> Mono<T> genericMonoFallback(Throwable ex) {
        return ReactiveFallbackHandler.handleMonoFallback("AccountService", ex);
    }

    @SuppressWarnings("unused")
    private <T> Flux<T> genericFluxFallback(Throwable ex) {
        return ReactiveFallbackHandler.handleFluxFallback("AccountService", ex);
    }

}
