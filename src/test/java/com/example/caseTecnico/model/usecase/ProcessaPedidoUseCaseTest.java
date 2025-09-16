package com.example.caseTecnico.model.usecase;

import com.example.caseTecnico.controller.in.dto.PedidoDto;
import com.example.caseTecnico.controller.out.RabbitMQProducer;
import com.example.caseTecnico.controller.out.mappers.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessaPedidoUseCaseTest {

    private RabbitMQProducer producer;
    private JsonSerializer jsonSerializer;
    private ProcessaPedidoUseCase useCase;

    private final String fila = "fila.teste";

    @BeforeEach
    void setUp() {
        producer = mock(RabbitMQProducer.class);
        jsonSerializer = mock(JsonSerializer.class);
        useCase = new ProcessaPedidoUseCase(producer, jsonSerializer);

        // Injetar valor da fila manualmente
        useCase.fila = fila;
    }

    @Test
    void testExecuta_ComPedidoValido_DeveEnviarMensagemERetornarAccepted() throws JsonProcessingException {
        PedidoDto pedido = new PedidoDto(UUID.randomUUID(), "ProdutoTeste", 10, null);
        when(jsonSerializer.toJson(pedido)).thenReturn("{\"id\":\"" + pedido.getId() + "\"}");

        var response = useCase.executa(pedido);

        assertEquals(202, response.getStatusCodeValue());
        assertEquals(pedido.getId().toString(), response.getBody());

        // Verifica se o producer foi chamado corretamente
        verify(producer, times(1)).sendAsync(fila, "{\"id\":\"" + pedido.getId() + "\"}");
    }

    @Test
    void testExecuta_ComQuantidadeInvalida_DeveRetornarBadRequest() {
        PedidoDto pedido = new PedidoDto(UUID.randomUUID(), "ProdutoTeste", 0, null);

        var response = useCase.executa(pedido);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Quantidade do pedido menor ou igual que zero.", response.getBody());

        verifyNoInteractions(producer);
        verifyNoInteractions(jsonSerializer);
    }

    @Test
    void testExecuta_ComProdutoVazio_DeveRetornarBadRequest() {
        PedidoDto pedido = new PedidoDto(UUID.randomUUID(), "", 10, null);

        var response = useCase.executa(pedido);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("O campo produto está vazio.", response.getBody());

        verifyNoInteractions(producer);
        verifyNoInteractions(jsonSerializer);
    }

    @Test
    void testExecuta_QuandoJsonProcessingException_DeveLancarRuntimeException() throws JsonProcessingException {
        PedidoDto pedido = new PedidoDto(UUID.randomUUID(), "ProdutoTeste", 10, null);
        when(jsonSerializer.toJson(pedido)).thenThrow(new JsonProcessingException("erro") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.executa(pedido));
        assertTrue(exception.getMessage().contains("Erro ao serializar mensagem"));

        verify(producer, never()).sendAsync(anyString(), anyString());
    }
}