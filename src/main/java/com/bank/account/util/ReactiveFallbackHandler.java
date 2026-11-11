package com.bank.account.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeoutException;

public final class ReactiveFallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(ReactiveFallbackHandler.class);

    private ReactiveFallbackHandler() {}

    public static <T> Mono<T> handleMonoFallback(String operationName, Throwable ex) {
        return Mono.error(mapException(operationName, ex));
    }

    public static <T> Flux<T> handleFluxFallback(String operationName, Throwable ex) {
        return Flux.error(mapException(operationName, ex));
    }

    // Método privado centralizado que traduce la excepción una sola vez
    private static ResponseStatusException mapException(String operationName, Throwable ex) {
        log.error("Fallback activado en operación [{}]. Causa: {} - {}", 
                operationName, ex.getClass().getSimpleName(), ex.getMessage());

        // 1. Si es una validación de negocio del banco, propagar sin ocultar el mensaje
        if (ex instanceof IllegalArgumentException) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        // 2. Si superó el TimeLimiter de 2 segundos
        if (ex instanceof TimeoutException) {
            return new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT, 
                    "La operación excedió el tiempo límite de 2 segundos.", ex);
        }

        // 3. Cualquier otro fallo de base de datos, Kafka o circuito abierto
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, 
                "El servicio no está disponible temporalmente. Intente nuevamente más tarde.", ex);
    }
}
