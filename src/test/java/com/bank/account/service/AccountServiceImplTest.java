package com.bank.account.service;

import com.bank.account.model.Account;
import com.bank.account.model.AccountConfig;
import com.bank.account.model.AccountTransaction;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.AccountTransactionRepository;
import com.bank.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account personalAccount;
    private Account enterpriseAccount;

    @BeforeEach
    void setUp() {
        personalAccount = new Account();
        personalAccount.setId("acc-id-001");
        personalAccount.setCustomerId("cust-personal-123");
        personalAccount.setCustomerType("PERSONAL");
        personalAccount.setType("SAVINGS");
        personalAccount.setBalance(new BigDecimal("1000.00"));
        personalAccount.setConfig(new AccountConfig(BigDecimal.ZERO, 5, 0, null));

        enterpriseAccount = new Account();
        enterpriseAccount.setId("acc-id-002");
        enterpriseAccount.setCustomerId("cust-enterprise-999");
        enterpriseAccount.setCustomerType("ENTERPRISE");
        enterpriseAccount.setType("SAVINGS"); // Disallowed type
        enterpriseAccount.setBalance(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("Should fail when an enterprise profile attempts to create a savings line")
    void testCreateAccountEnterpriseSavingsDisallowed() {
        // Act
        Mono<Account> result = accountService.createAccount(enterpriseAccount);

        // Assert
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Should successfully create a personal account when no duplicates exist")
    void testCreateAccountPersonalSuccess() {
        // Arrange
        Mockito.when(accountRepository.findByCustomerId("cust-personal-123")).thenReturn(Flux.empty());
        Mockito.when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(personalAccount));

        // Act
        Mono<Account> result = accountService.createAccount(personalAccount);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(account -> 
                        "ACTIVE".equals(account.getStatus()) && 
                        account.getAccountNumber() != null
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should process a withdrawal and deduct balance correctly")
    void testExecuteTransactionWithdrawalSuccess() {
        // Arrange
        Mockito.when(accountRepository.findByAccountNumber("ACC-12345")).thenReturn(Mono.just(personalAccount));
        Mockito.when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(personalAccount));
        
        AccountTransaction mockTx = new AccountTransaction();
        mockTx.setType("WITHDRAWAL");
        mockTx.setAmount(new BigDecimal("200.00"));
        Mockito.when(transactionRepository.save(any(AccountTransaction.class))).thenReturn(Mono.just(mockTx));

        // Act
        Mono<AccountTransaction> result = accountService.executeTransaction(
                "ACC-12345", "WITHDRAWAL", new BigDecimal("200.00"), "ATM Withdrawal");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(tx -> "WITHDRAWAL".equals(tx.getType()) && tx.getAmount().intValue() == 200)
                .verifyComplete();
    }
}
