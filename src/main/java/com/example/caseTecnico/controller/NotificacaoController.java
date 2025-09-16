package com.example.caseTecnico.controller;

import com.example.caseTecnico.controller.in.dto.PedidoDto;
import com.example.caseTecnico.model.usecase.ProcessaPedidoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NotificacaoController {

    private final ProcessaPedidoUseCase processaPedidoUseCase;

    public NotificacaoController(ProcessaPedidoUseCase processaPedidoUseCase) {
        this.processaPedidoUseCase = processaPedidoUseCase;
    }

    @PostMapping("/pedidos")
    public ResponseEntity<String> post(@RequestBody PedidoDto mensagem) {
        return processaPedidoUseCase.executa(mensagem);
    }
}
