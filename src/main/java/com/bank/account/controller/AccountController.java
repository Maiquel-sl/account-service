package com.bank.account.controller;

import com.bank.account.model.Account;
import com.bank.account.model.AccountTransaction;
import com.bank.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping("/number/{accountNumber}")
    public Mono<ResponseEntity<Account>> getAccountByNumber(@PathVariable String accountNumber) {
        return accountService.getAccountByNumber(accountNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public Flux<Account> getAccountsByCustomer(@PathVariable String customerId) {
        return accountService.getAccountsByCustomerId(customerId);
    }

    // Endpoint para realizar Depósitos y Retiros directos
    @PostMapping("/transaction")
    public Mono<ResponseEntity<AccountTransaction>> executeTransaction(@RequestBody Map<String, Object> payload) {
        String accountNumber = (String) payload.get("accountNumber");
        String type = (String) payload.get("type"); // DEPOSIT o WITHDRAWAL
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String description = (String) payload.get("description");

        return accountService.executeTransaction(accountNumber, type, amount, description)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, ex -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
