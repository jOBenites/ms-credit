package com.bank.mscredit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento financiero registrado sobre un credito.
 * Tipo: pago de credito (CREDIT_PAYMENT). Cada movimiento queda
 * asociado a su credito y se publica como evento bank.movement.recorded.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "movement")
public class Movement {

    /** Tipo de movimiento: pago de credito. */
    public static final String TYPE_CREDIT_PAYMENT = "CREDIT_PAYMENT";

    @Id
    private String id;

    @Indexed
    private String creditId;

    private String movementType;

    private BigDecimal amount;

    private LocalDateTime occurredAt;

    /**
     * Constructor para crear un movimiento nuevo.
     * La fecha de ocurrencia se fija al momento actual.
     *
     * @param creditId identificador del credito afectado
     * @param movementType tipo de movimiento (CREDIT_PAYMENT)
     * @param amount monto del movimiento (mayor a cero)
     */
    public Movement(String creditId, String movementType, BigDecimal amount) {
        this.creditId = creditId;
        this.movementType = movementType;
        this.amount = amount;
        this.occurredAt = LocalDateTime.now();
    }
}
