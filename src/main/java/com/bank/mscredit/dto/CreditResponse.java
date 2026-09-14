package com.bank.mscredit.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta de un credito.
 */
@Getter
@Setter
public class CreditResponse {

    private String id;
    private String customerId;
    private String creditType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal outstandingBalance;
    private String status;
}
