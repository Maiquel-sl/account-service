package com.bank.account.repository;

import com.bank.account.model.CustomerReplica;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerReplicaRepository extends ReactiveMongoRepository<CustomerReplica, String> {
}
