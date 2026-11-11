package com.bank.account.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "customer_replicas")
public class CustomerReplica {
    @Id
    private String customerId;
    private String type;   // PERSONAL, ENTERPRISE
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime lastSyncedAt;

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
