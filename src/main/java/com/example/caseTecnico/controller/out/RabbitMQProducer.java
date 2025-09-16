package com.example.caseTecnico.controller.out;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Async
    public CompletableFuture<Void> sendAsync(String fila, String mensagem) {
        return CompletableFuture.runAsync(() -> {
            log.info("Enviando pedido para fila {}: {}", fila, mensagem);
            rabbitTemplate.convertAndSend(fila, mensagem);
            log.info("Pedido enviado com sucesso!");
        });
    }
}