package com.bank.mscredit.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de solicitud para registrar un pago sobre un credito.
 */
@Getter
@Setter
public class MovementRequest {

    private BigDecimal amount;
}
