package com.bank.mscredit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un credito bancario (producto activo).
 * El tipo de credito debe ser consistente con el tipo de cliente:
 * PERSONAL (maximo 1 por cliente personal) o BUSINESS (N por cliente empresarial).
 * Un credito es independiente de que el cliente tenga cuentas bancarias.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "credit")
public class Credit {

    /** Tipo de credito personal. */
    public static final String TYPE_PERSONAL = "PERSONAL";
    /** Tipo de credito empresarial. */
    public static final String TYPE_BUSINESS = "BUSINESS";
    /** Estado de credito activo. */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /** Estado de credito pagado. */
    public static final String STATUS_PAID = "PAID";

    @Id
    private String id;

    @Indexed
    private String customerId;

    private String creditType;

    private BigDecimal amount;

    private BigDecimal interestRate;

    private Integer termMonths;

    private BigDecimal outstandingBalance;

    private String status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Constructor para crear un credito nuevo.
     * El saldo pendiente inicia igual al monto otorgado y el estado en ACTIVE.
     *
     * @param customerId identificador del cliente
     * @param creditType tipo de credito (PERSONAL o BUSINESS)
     * @param amount monto otorgado
     * @param interestRate tasa de interes
     * @param termMonths plazo en meses
     */
    public Credit(String customerId, String creditType, BigDecimal amount,
                  BigDecimal interestRate, Integer termMonths) {
        this.customerId = customerId;
        this.creditType = creditType;
        this.amount = amount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.outstandingBalance = amount;
        this.status = STATUS_ACTIVE;
    }
}
