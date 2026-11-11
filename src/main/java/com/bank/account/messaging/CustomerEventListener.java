package com.bank.account.messaging;

import com.bank.account.events.CustomerEvent;
import com.bank.account.model.CustomerReplica;
import com.bank.account.repository.CustomerReplicaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.function.Function;

@Configuration
public class CustomerEventListener {

    @Bean
    public Function<Flux<CustomerEvent>, Mono<Void>> customerEventConsumer(CustomerReplicaRepository repository) {
        return flux -> flux.flatMap(event -> {
            CustomerReplica replica = new CustomerReplica();
            replica.setCustomerId(event.customerId());
            replica.setType(event.type());
            replica.setStatus(event.status());
            replica.setLastSyncedAt(LocalDateTime.now());
            
            return repository.save(replica);
        }).then();
    }
}
