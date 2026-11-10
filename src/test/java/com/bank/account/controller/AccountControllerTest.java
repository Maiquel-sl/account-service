package com.bank.account.controller;

import com.bank.account.model.Account;
import com.bank.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@WebFluxTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("GET /api/accounts/number/{accountNumber} - Should return account with HTTP 200 OK")
    void testGetAccountByNumberFound() {
        Account mockAccount = new Account();
        mockAccount.setAccountNumber("ACC-TEST-88");
        mockAccount.setBalance(new BigDecimal("500.00"));

        Mockito.when(accountService.getAccountByNumber("ACC-TEST-88")).thenReturn(Mono.just(mockAccount));

        webTestClient.get()
                .uri("/api/accounts/number/ACC-TEST-88")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accountNumber").isEqualTo("ACC-TEST-88")
                .jsonPath("$.balance").isEqualTo(500.00);
    }

    @Test
    @DisplayName("GET /api/accounts/customer/{customerId} - Should stream customer portfolio items")
    void testGetAccountsByCustomer() {
        Account mockAccount = new Account();
        mockAccount.setCustomerId("cust-100");

        Mockito.when(accountService.getAccountsByCustomerId("cust-100")).thenReturn(Flux.just(mockAccount));

        webTestClient.get()
                .uri("/api/accounts/customer/cust-100")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Account.class)
                .hasSize(1);
    }
}
