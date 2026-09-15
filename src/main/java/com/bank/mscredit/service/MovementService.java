package com.bank.mscredit.service;

import com.bank.mscredit.dto.MovementResponse;
import com.bank.mscredit.event.CreditEventProducer;
import com.bank.mscredit.model.Credit;
import com.bank.mscredit.model.Movement;
import com.bank.mscredit.repository.CreditRepository;
import com.bank.mscredit.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de registro de movimientos sobre creditos.
 * Permite registrar pagos de credito y consultar el historial.
 * Si el saldo pendiente llega a cero, el credito cambia a estado PAID.
 * Cada movimiento registrado se publica como evento bank.movement.recorded.
 */
@Service
@RequiredArgsConstructor
public class MovementService {

    private final CreditRepository creditRepository;
    private final MovementRepository movementRepository;
    private final CreditEventProducer creditEventProducer;

    /**
     * Registra un pago sobre un credito activo.
     * Reduce el saldo pendiente; si llega a cero, cambia estado a PAID.
     *
     * @param creditId identificador del credito
     * @param amount monto del pago (mayor a cero)
     * @return el movimiento registrado, o empty si el credito no existe
     * @throws IllegalArgumentException si el monto no es positivo, el credito
     *         no esta activo o el monto supera el saldo pendiente
     */
    public Optional<Movement> pay(String creditId, BigDecimal amount) {
        requirePositiveAmount(amount);
        return creditRepository.findById(creditId).map(credit -> {
            requireActiveCredit(credit);
            if (amount.compareTo(credit.getOutstandingBalance()) > 0) {
                throw new IllegalArgumentException("El monto del pago supera el saldo pendiente del credito");
            }
            credit.setOutstandingBalance(credit.getOutstandingBalance().subtract(amount));
            if (credit.getOutstandingBalance().signum() == 0) {
                credit.setStatus(Credit.STATUS_PAID);
            }
            creditRepository.save(credit);
            Movement movement = movementRepository.save(
                    new Movement(creditId, Movement.TYPE_CREDIT_PAYMENT, amount));
            creditEventProducer.publishMovementRecorded(movement, credit.getCreditType());
            return movement;
        });
    }

    /**
     * Lista los movimientos de un credito del mas reciente al mas antiguo.
     *
     * @param creditId identificador del credito
     * @return lista de movimientos, o empty si el credito no existe
     */
    public Optional<List<Movement>> findMovements(String creditId) {
        if (!creditRepository.existsById(creditId)) {
            return Optional.empty();
        }
        return Optional.of(movementRepository.findByCreditIdOrderByOccurredAtDesc(creditId));
    }

    /**
     * Convierte una entidad Movement a su DTO de respuesta.
     *
     * @param movement entidad a convertir
     * @return DTO con los campos poblados
     */
    public MovementResponse toMovementResponse(Movement movement) {
        MovementResponse response = new MovementResponse();
        response.setId(movement.getId());
        response.setCreditId(movement.getCreditId());
        response.setMovementType(movement.getMovementType());
        response.setAmount(movement.getAmount());
        response.setOccurredAt(movement.getOccurredAt());
        return response;
    }

    /**
     * Convierte una lista de entidades Movement a DTOs de respuesta.
     *
     * @param movements lista de entidades
     * @return lista de DTOs
     */
    public List<MovementResponse> toMovementResponseList(List<Movement> movements) {
        return movements.stream()
                .map(this::toMovementResponse)
                .collect(Collectors.toList());
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private void requireActiveCredit(Credit credit) {
        if (!Credit.STATUS_ACTIVE.equals(credit.getStatus())) {
            throw new IllegalArgumentException("El credito no esta activo");
        }
    }
}
