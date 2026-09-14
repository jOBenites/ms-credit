package com.bank.mscredit.service;

import com.bank.mscredit.dto.CreditResponse;
import com.bank.mscredit.event.CreditEventProducer;
import com.bank.mscredit.model.Credit;
import com.bank.mscredit.model.CustomerView;
import com.bank.mscredit.repository.CreditRepository;
import com.bank.mscredit.repository.CustomerViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
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
 * Pruebas unitarias para {@link CreditService}.
 * Valida las reglas de cardinalidad y consistencia de tipo, el CRUD completo
 * y la publicacion de eventos de dominio.
 */
@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CustomerViewRepository customerViewRepository;

    @Mock
    private CreditEventProducer creditEventProducer;

    @InjectMocks
    private CreditService creditService;

    private CustomerView personalView;
    private CustomerView businessView;
    private Credit personalCredit;
    private Credit businessCredit;

    @BeforeEach
    void setUp() {
        personalView = new CustomerView("cust-1", "PERSONAL", "REGULAR", "12345678");
        businessView = new CustomerView("cust-2", "BUSINESS", "REGULAR", "87654321");

        personalCredit = new Credit("cust-1", Credit.TYPE_PERSONAL,
                new BigDecimal("10000.00"), new BigDecimal("0.12"), 24);
        personalCredit.setId("cred-1");

        businessCredit = new Credit("cust-2", Credit.TYPE_BUSINESS,
                new BigDecimal("50000.00"), new BigDecimal("0.10"), 36);
        businessCredit.setId("cred-2");
    }

    @Test
    void grantCredit_personal_success() {
        when(customerViewRepository.findById("cust-1")).thenReturn(Optional.of(personalView));
        when(creditRepository.countByCustomerIdAndCreditType("cust-1", Credit.TYPE_PERSONAL)).thenReturn(0L);
        when(creditRepository.save(any(Credit.class))).thenReturn(personalCredit);

        Credit result = creditService.grantCredit("cust-1", Credit.TYPE_PERSONAL,
                new BigDecimal("10000.00"), new BigDecimal("0.12"), 24);

        assertNotNull(result);
        assertEquals(Credit.TYPE_PERSONAL, result.getCreditType());
        assertEquals(Credit.STATUS_ACTIVE, result.getStatus());
        assertEquals(result.getAmount(), result.getOutstandingBalance());
        verify(creditEventProducer).publishCreditGranted(personalCredit);
    }

    @Test
    void grantCredit_personalAlreadyHasCredit_throws() {
        when(customerViewRepository.findById("cust-1")).thenReturn(Optional.of(personalView));
        when(creditRepository.countByCustomerIdAndCreditType("cust-1", Credit.TYPE_PERSONAL)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> creditService.grantCredit(
                "cust-1", Credit.TYPE_PERSONAL, new BigDecimal("5000"), null, 12));
        verify(creditRepository, never()).save(any());
        verify(creditEventProducer, never()).publishCreditGranted(any());
    }

    @Test
    void grantCredit_personalCreditForBusinessCustomer_throws() {
        when(customerViewRepository.findById("cust-2")).thenReturn(Optional.of(businessView));

        assertThrows(IllegalArgumentException.class, () -> creditService.grantCredit(
                "cust-2", Credit.TYPE_PERSONAL, new BigDecimal("5000"), null, 12));
        verify(creditRepository, never()).countByCustomerIdAndCreditType(anyString(), anyString());
        verify(creditRepository, never()).save(any());
    }

    @Test
    void grantCredit_business_success() {
        when(customerViewRepository.findById("cust-2")).thenReturn(Optional.of(businessView));
        when(creditRepository.save(any(Credit.class))).thenReturn(businessCredit);

        Credit result = creditService.grantCredit("cust-2", Credit.TYPE_BUSINESS,
                new BigDecimal("50000.00"), new BigDecimal("0.10"), 36);

        assertNotNull(result);
        assertEquals(Credit.TYPE_BUSINESS, result.getCreditType());
        verify(creditRepository, never()).countByCustomerIdAndCreditType(anyString(), anyString());
        verify(creditEventProducer).publishCreditGranted(businessCredit);
    }

    @Test
    void grantCredit_businessCreditForPersonalCustomer_throws() {
        when(customerViewRepository.findById("cust-1")).thenReturn(Optional.of(personalView));

        assertThrows(IllegalArgumentException.class, () -> creditService.grantCredit(
                "cust-1", Credit.TYPE_BUSINESS, new BigDecimal("5000"), null, 12));
        verify(creditRepository, never()).save(any());
    }

    @Test
    void grantCredit_invalidType_throws() {
        when(customerViewRepository.findById("cust-1")).thenReturn(Optional.of(personalView));

        assertThrows(IllegalArgumentException.class, () -> creditService.grantCredit(
                "cust-1", "MORTGAGE", new BigDecimal("5000"), null, 12));
        verify(creditRepository, never()).save(any());
    }

    @Test
    void grantCredit_nonPositiveAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> creditService.grantCredit(
                "cust-1", Credit.TYPE_PERSONAL, BigDecimal.ZERO, null, 12));
        verify(customerViewRepository, never()).findById(anyString());
        verify(creditRepository, never()).save(any());
    }

    @Test
    void grantCredit_customerNotFound_throws() {
        when(customerViewRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> creditService.grantCredit(
                "unknown", Credit.TYPE_PERSONAL, new BigDecimal("5000"), null, 12));
        verify(creditRepository, never()).save(any());
    }

    @Test
    void findById_found() {
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(personalCredit));

        Optional<Credit> result = creditService.findById("cred-1");

        assertTrue(result.isPresent());
        assertEquals("cred-1", result.get().getId());
    }

    @Test
    void findById_notFound() {
        when(creditRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Credit> result = creditService.findById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_returnsList() {
        when(creditRepository.findAll()).thenReturn(Arrays.asList(personalCredit, businessCredit));

        List<Credit> result = creditService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void update_found_updatesFields() {
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(personalCredit));
        when(creditRepository.save(any(Credit.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Credit> result = creditService.update("cred-1", new BigDecimal("0.15"), 30);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("0.15"), result.get().getInterestRate());
        assertEquals(30, result.get().getTermMonths());
    }

    @Test
    void update_found_partialUpdate() {
        when(creditRepository.findById("cred-1")).thenReturn(Optional.of(personalCredit));
        when(creditRepository.save(any(Credit.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Credit> result = creditService.update("cred-1", null, 30);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("0.12"), result.get().getInterestRate());
        assertEquals(30, result.get().getTermMonths());
    }

    @Test
    void update_notFound() {
        when(creditRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Credit> result = creditService.update("nonexistent", new BigDecimal("0.15"), 30);

        assertFalse(result.isPresent());
        verify(creditRepository, never()).save(any());
    }

    @Test
    void delete_found_returnsTrue() {
        when(creditRepository.existsById("cred-1")).thenReturn(true);

        boolean result = creditService.delete("cred-1");

        assertTrue(result);
        verify(creditRepository).deleteById("cred-1");
    }

    @Test
    void delete_notFound_returnsFalse() {
        when(creditRepository.existsById("nonexistent")).thenReturn(false);

        boolean result = creditService.delete("nonexistent");

        assertFalse(result);
        verify(creditRepository, never()).deleteById(anyString());
    }

    @Test
    void toResponse_mapsAllFields() {
        CreditResponse response = creditService.toResponse(personalCredit);

        assertEquals("cred-1", response.getId());
        assertEquals("cust-1", response.getCustomerId());
        assertEquals(Credit.TYPE_PERSONAL, response.getCreditType());
        assertEquals(new BigDecimal("10000.00"), response.getAmount());
        assertEquals(new BigDecimal("10000.00"), response.getOutstandingBalance());
        assertEquals(Credit.STATUS_ACTIVE, response.getStatus());
    }

    @Test
    void toResponseList() {
        List<CreditResponse> responses = creditService.toResponseList(
                Arrays.asList(personalCredit, businessCredit));

        assertEquals(2, responses.size());
    }
}
