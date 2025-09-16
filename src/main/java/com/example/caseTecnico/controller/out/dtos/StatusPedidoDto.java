package com.example.caseTecnico.controller.out.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusPedidoDto {

    private UUID idPedido;
    private StatusEnum status;
    private LocalDateTime dataProcessamento;
}
