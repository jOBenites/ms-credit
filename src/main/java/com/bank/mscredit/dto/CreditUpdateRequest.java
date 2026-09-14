package com.bank.mscredit.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de solicitud para actualizar las condiciones de un credito.
 */
@Getter
@Setter
public class CreditUpdateRequest {

    private BigDecimal interestRate;
    private Integer termMonths;
}
