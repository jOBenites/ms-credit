package com.bank.mscredit.service;

import com.bank.mscredit.event.CreditEventProducer;
import com.bank.mscredit.model.Credit;
import com.bank.mscredit.model.Movement;
import com.bank.mscredit.repository.CreditRepository;
import com.bank.mscredit.repository.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link MovementService} (creditos).
 * Valida registro de pagos, validaciones de saldo y estado del credito.
 */
@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private CreditEventProducer creditEventProducer;

    @InjectMocks
    private MovementService movementService;

    private Credit activeCredit;
    private Movement paymentMovement;

    @BeforeEach
    void setUp() {
        activeCredit = new Credit("cust-1", Credit.TYPE_PERSONAL,
                new BigDecimal("10000.00"), new BigDecimal("0.12"), 24);
        activeCredit.setId("cred-1");
        activeCredit.setOutstandingBalance(new BigDecimal("10000.00"));

        paymentMovement = new Movement("cred-1", Movement.TYPE_CREDIT_PAYMENT, new BigDecimal("500.00"));
        paymentMovement.setId("mov-1");
    }

    @Test
    void pay_success() {
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(activeCredit));
        when(creditRepository.save(any(Credit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any(Movement.class))).thenReturn(paymentMovement);

        Optional<Movement> result = movementService.pay("cred-1", new BigDecimal("500.00"));

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("9500.00"), activeCredit.getOutstandingBalance());
        assertEquals(Credit.STATUS_ACTIVE, activeCredit.getStatus());
        verify(creditEventProducer).publishMovementRecorded(paymentMovement, Credit.TYPE_PERSONAL);
    }

    @Test
    void pay_fullPayment_setsPaidStatus() {
        activeCredit.setOutstandingBalance(new BigDecimal("500.00"));
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(activeCredit));
        when(creditRepository.save(any(Credit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any(Movement.class))).thenReturn(paymentMovement);

        Optional<Movement> result = movementService.pay("cred-1", new BigDecimal("500.00"));

        assertTrue(result.isPresent());
        assertEquals(0, activeCredit.getOutstandingBalance().compareTo(BigDecimal.ZERO));
        assertEquals(Credit.STATUS_PAID, activeCredit.getStatus());
    }

    @Test
    void pay_creditNotFound_returnsEmpty() {
        when(creditRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Movement> result = movementService.pay("nonexistent", new BigDecimal("500.00"));

        assertFalse(result.isPresent());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void pay_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> movementService.pay("cred-1", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> movementService.pay("cred-1", new BigDecimal("-100")));
        verify(creditRepository, never()).findById(anyString());
    }

    @Test
    void pay_inactiveCredit_throws() {
        activeCredit.setStatus(Credit.STATUS_PAID);
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(activeCredit));

        assertThrows(IllegalArgumentException.class,
                () -> movementService.pay("cred-1", new BigDecimal("500.00")));
        verify(movementRepository, never()).save(any());
    }

    @Test
    void pay_amountExceedsOutstandingBalance_throws() {
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(activeCredit));

        assertThrows(IllegalArgumentException.class,
                () -> movementService.pay("cred-1", new BigDecimal("20000.00")));
        verify(movementRepository, never()).save(any());
    }

    @Test
    void findMovements_creditNotFound_returnsEmpty() {
        when(creditRepository.existsById("nonexistent")).thenReturn(false);

        Optional<List<Movement>> result = movementService.findMovements("nonexistent");

        assertFalse(result.isPresent());
        verify(movementRepository, never()).findByCreditIdOrderByOccurredAtDesc(anyString());
    }

    @Test
    void findMovements_creditExists_returnsMovements() {
        when(creditRepository.existsById("cred-1")).thenReturn(true);
        when(movementRepository.findByCreditIdOrderByOccurredAtDesc("cred-1"))
                .thenReturn(List.of(paymentMovement));

        Optional<List<Movement>> result = movementService.findMovements("cred-1");

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
    }

    @Test
    void toMovementResponse_mapsFields() {
        var response = movementService.toMovementResponse(paymentMovement);

        assertEquals("mov-1", response.getId());
        assertEquals("cred-1", response.getCreditId());
        assertEquals(Movement.TYPE_CREDIT_PAYMENT, response.getMovementType());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertNotNull(response.getOccurredAt());
    }
}
