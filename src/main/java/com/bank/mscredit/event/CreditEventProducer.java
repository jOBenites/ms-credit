package com.bank.mscredit.event;

import com.bank.mscredit.model.Credit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Productor de eventos Kafka para el dominio credit.
 * Publica bank.credit.granted cuando se otorga un nuevo credito.
 */
@Component
@RequiredArgsConstructor
public class CreditEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CreditEventProducer.class);
    private static final String CREDIT_GRANTED_TOPIC = "bank.credit.granted";

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
}
