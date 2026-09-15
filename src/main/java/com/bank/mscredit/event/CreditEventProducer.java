package com.bank.mscredit.event;

import com.bank.mscredit.model.Credit;
import com.bank.mscredit.model.Movement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Productor de eventos Kafka para el dominio credit.
 * Publica bank.credit.granted cuando se otorga un nuevo credito y
 * bank.movement.recorded cuando se registra un pago.
 */
@Component
@RequiredArgsConstructor
public class CreditEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CreditEventProducer.class);
    private static final String CREDIT_GRANTED_TOPIC = "bank.credit.granted";
    private static final String MOVEMENT_RECORDED_TOPIC = "bank.movement.recorded";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publica el evento bank.credit.granted con los datos minimos del credito.
     *
     * @param credit credito recien otorgado
     */
    public void publishCreditGranted(Credit credit) {
        Map<String, Object> payload = Map.of(
                "creditId", credit.getId(),
                "customerId", credit.getCustomerId(),
                "creditType", credit.getCreditType(),
                "amount", credit.getAmount(),
                "occurredAt", LocalDateTime.now()
        );
        kafkaTemplate.send(CREDIT_GRANTED_TOPIC, credit.getId(), payload);
        log.info("Evento bank.credit.granted publicado para credito {}", credit.getId());
    }

    /**
     * Publica el evento bank.movement.recorded con los datos del movimiento.
     *
     * @param movement movimiento recien registrado
     * @param productType tipo de credito afectado (PERSONAL o BUSINESS)
     */
    public void publishMovementRecorded(Movement movement, String productType) {
        Map<String, Object> payload = Map.of(
                "movementId", movement.getId(),
                "productId", movement.getCreditId(),
                "productType", productType,
                "movementType", movement.getMovementType(),
                "amount", movement.getAmount(),
                "occurredAt", movement.getOccurredAt()
        );
        kafkaTemplate.send(MOVEMENT_RECORDED_TOPIC, movement.getId(), payload);
        log.info("Evento bank.movement.recorded publicado para movimiento {} sobre credito {}",
                movement.getId(), movement.getCreditId());
    }
}
