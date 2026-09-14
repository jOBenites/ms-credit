package com.bank.mscredit.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de solicitud para el otorgamiento de un credito.
 * El tipo de credito debe ser consistente con el tipo de cliente.
 */
@Getter
@Setter
public class GrantCreditRequest {

    private String customerId;
    private String creditType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
}
