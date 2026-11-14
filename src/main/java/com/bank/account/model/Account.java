package com.bank.account.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private String accountNumber;
    @Field(targetType = FieldType.OBJECT_ID)
    private String customerId;
    private String customerType; // PERSONAL, ENTERPRISE
    private String type; // SAVINGS, CURRENT, FIXED_TERM
    private String status; // ACTIVE, INACTIVE
    private BigDecimal balance;
    private String currency;
    private AccountConfig config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters y Setters de Java 27
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public AccountConfig getConfig() { return config; }
    public void setConfig(AccountConfig config) { this.config = config; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
