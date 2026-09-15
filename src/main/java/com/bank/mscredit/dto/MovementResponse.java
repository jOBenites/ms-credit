package com.bank.mscredit.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta de un movimiento registrado sobre un credito.
 */
@Getter
@Setter
public class MovementResponse {

    private String id;
    private String creditId;
    private String movementType;
    private BigDecimal amount;
    private LocalDateTime occurredAt;
}
