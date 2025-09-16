package com.example.caseTecnico.model.usecase;

import com.example.caseTecnico.controller.in.dto.PedidoDto;
import com.example.caseTecnico.controller.out.RabbitMQProducer;
import com.example.caseTecnico.controller.out.mappers.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessaPedidoUseCase {

    private final RabbitMQProducer producer;
    private final JsonSerializer jsonSerializer;

    @Value("${rabbitmq.queue}")
    String fila;

    public ResponseEntity<String> executa(PedidoDto mensagem) {


        return validaPedido(mensagem)
                .orElseGet(() -> {
                    try {
                        producer.sendAsync(fila, jsonSerializer.toJson(mensagem));
                        return ResponseEntity.accepted().body(mensagem.getId().toString());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Erro ao serializar mensagem", e);
                    }
                });
    }

    private Optional<ResponseEntity<String>> validaPedido(PedidoDto mensagemDto) {
        if (mensagemDto.getQuantidade() <= 0) {
            return Optional.of(ResponseEntity.badRequest()
                    .body("Quantidade do pedido menor ou igual que zero."));
        }
        if (mensagemDto.getProduto() == null || mensagemDto.getProduto().isEmpty()) {
            return Optional.of(ResponseEntity.badRequest()
                    .body("O campo produto está vazio."));
        }
        return Optional.empty();
    }
}
