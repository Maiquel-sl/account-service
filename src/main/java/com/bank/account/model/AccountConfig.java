package com.bank.account.model;

import java.math.BigDecimal;

public record AccountConfig(
    BigDecimal maintenanceFee,
    Integer maxMonthlyMovements,
    Integer currentMonthlyMovements,
    Integer allowedOperationDay
) {}
