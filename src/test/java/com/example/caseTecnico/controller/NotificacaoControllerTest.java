package com.example.caseTecnico.controller;

import com.example.caseTecnico.controller.in.dto.PedidoDto;
import com.example.caseTecnico.model.usecase.ProcessaPedidoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacaoControllerTest {
    @Mock
    private ProcessaPedidoUseCase processaPedidoUseCase;

    @InjectMocks
    private NotificacaoController notificacaoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPostPedido_Success() {
        // Arrange
        PedidoDto pedido = new PedidoDto();
        pedido.setId(UUID.randomUUID());
        pedido.setProduto("Produto A");
        pedido.setQuantidade(10);

        ResponseEntity<String> expectedResponse = ResponseEntity.accepted().body(pedido.getId().toString());

        when(processaPedidoUseCase.executa(pedido)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = notificacaoController.post(pedido);

        // Assert
        assertEquals(expectedResponse, response);
        verify(processaPedidoUseCase, times(1)).executa(pedido);
    }

    @Test
    void testPostPedido_BadRequest() {
        // Arrange
        PedidoDto pedido = new PedidoDto();
        pedido.setId(UUID.randomUUID());
        pedido.setProduto("");
        pedido.setQuantidade(0);

        ResponseEntity<String> expectedResponse = ResponseEntity.badRequest().body("Quantidade do pedido menor ou igual que zero.");

        when(processaPedidoUseCase.executa(pedido)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = notificacaoController.post(pedido);

        // Assert
        assertEquals(expectedResponse, response);
        verify(processaPedidoUseCase, times(1)).executa(pedido);
    }
}