package com.bank.account.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountTransactionEvent(
    String transactionNumber,
    String customerId,
    String accountId,
    String accountSubType, // SAVINGS, CURRENT, FIXED_TERM
    String accountNumber,
    String operationType,  // DEPOSIT, WITHDRAWAL
    BigDecimal amount,
    String currency,
    String description,
    LocalDateTime timestamp
) {}