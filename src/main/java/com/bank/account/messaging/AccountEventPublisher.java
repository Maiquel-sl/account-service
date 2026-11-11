package com.bank.account.messaging;

import com.bank.account.events.AccountTransactionEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.function.Supplier;

@Component
public class AccountEventPublisher {

    //Usamos Sinks.Many de Project Reactor para emitir eventos de forma no bloqueante hacia Kafka cada vez que ocurre una transacción.
    private final Sinks.Many<AccountTransactionEvent> sink = 
            Sinks.many().multicast().onBackpressureBuffer();

    public void publish(AccountTransactionEvent event) {
        sink.tryEmitNext(event);
    }

    @Bean
    public Supplier<Flux<AccountTransactionEvent>> accountEventProducer() {
        return sink::asFlux;
    }
}