package com.example.caseTecnico.controller.in;

import com.example.caseTecnico.controller.exceptions.ExcecaoDeProcessamento;
import com.example.caseTecnico.controller.in.dto.PedidoDto;
import com.example.caseTecnico.controller.out.RabbitMQProducer;
import com.example.caseTecnico.controller.out.dtos.StatusEnum;
import com.example.caseTecnico.controller.out.dtos.StatusPedidoDto;
import com.example.caseTecnico.controller.out.mappers.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final RabbitMQProducer producer;
    private final JsonSerializer jsonSerializer;

    @Value("${rabbitmq.queue}")
    private String fila;

    @Value("${rabbitmq.queue.sucesso}")
    private String filaSucesso;

    @Value("${rabbitmq.queue.falha}")
    private String filaFalha;

    @Value("${rabbitmq.queue.dlq}")
    private String dlq;

    @Async
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void receive(String mensagem) throws JsonProcessingException {

        PedidoDto mensagemConvertida = jsonSerializer.fromJson(mensagem, PedidoDto.class);

        log.info("Recebido pedido da fila {}: {}", fila, mensagem);
        double aleatorio = ThreadLocalRandom.current().nextDouble();

        try {
            long delay = ThreadLocalRandom.current().nextLong(1000, 3000);
            Thread.sleep(delay);

            if (aleatorio < 0.2) {
                StatusPedidoDto statusPedidoDto = new StatusPedidoDto(
                        mensagemConvertida.getId(),
                        StatusEnum.FALHA,
                        LocalDateTime.now()
                );

                producer.sendAsync(filaFalha, jsonSerializer.toJson(statusPedidoDto));
                producer.sendAsync(dlq, jsonSerializer.toJson(mensagem));

                log.error("Processamento do pedido {} falhou (aleatório < 0.2)", mensagemConvertida.getId());
                throw new ExcecaoDeProcessamento("O número aleatório é menor que 0.2");
            } else {
                StatusPedidoDto statusPedidoDto = new StatusPedidoDto(
                        mensagemConvertida.getId(),
                        StatusEnum.SUCESSO,
                        LocalDateTime.now()
                );

                log.info("Processamento concluído para o pedido: {}", mensagemConvertida.getId());
                producer.sendAsync(filaSucesso, jsonSerializer.toJson(statusPedidoDto));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processamento interrompido para o pedido: {}", mensagemConvertida.getId(), e);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar mensagem para fila: {}", mensagemConvertida.getId(), e);
        } catch (ExcecaoDeProcessamento e) {
            throw new RuntimeException(e);
        }
    }
}