package com.bank.mscredit.controller;

import com.bank.mscredit.dto.CreditResponse;
import com.bank.mscredit.dto.CreditUpdateRequest;
import com.bank.mscredit.dto.GrantCreditRequest;
import com.bank.mscredit.model.Credit;
import com.bank.mscredit.service.CreditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link CreditController}.
 * Valida los endpoints REST y los codigos de respuesta HTTP.
 */
@ExtendWith(MockitoExtension.class)
class CreditControllerTest {

    @Mock
    private CreditService creditService;

    @InjectMocks
    private CreditController creditController;

    private Credit personalCredit;
    private Credit businessCredit;
    private CreditResponse personalResponse;
    private CreditResponse businessResponse;

    @BeforeEach
    void setUp() {
        personalCredit = new Credit("cust-1", Credit.TYPE_PERSONAL,
                new BigDecimal("10000.00"), new BigDecimal("0.12"), 24);
        personalCredit.setId("cred-1");

        businessCredit = new Credit("cust-2", Credit.TYPE_BUSINESS,
                new BigDecimal("50000.00"), new BigDecimal("0.10"), 36);
        businessCredit.setId("cred-2");

        personalResponse = new CreditResponse();
        personalResponse.setId("cred-1");
        personalResponse.setCustomerId("cust-1");
        personalResponse.setCreditType(Credit.TYPE_PERSONAL);

        businessResponse = new CreditResponse();
        businessResponse.setId("cred-2");
        businessResponse.setCustomerId("cust-2");
        businessResponse.setCreditType(Credit.TYPE_BUSINESS);
    }

    @Test
    void grantCredit_returns201() {
        when(creditService.grantCredit("cust-1", Credit.TYPE_PERSONAL,
                new BigDecimal("10000.00"), new BigDecimal("0.12"), 24))
                .thenReturn(personalCredit);
        when(creditService.toResponse(personalCredit)).thenReturn(personalResponse);

        GrantCreditRequest request = new GrantCreditRequest();
        request.setCustomerId("cust-1");
        request.setCreditType(Credit.TYPE_PERSONAL);
        request.setAmount(new BigDecimal("10000.00"));
        request.setInterestRate(new BigDecimal("0.12"));
        request.setTermMonths(24);
        ResponseEntity<CreditResponse> response = creditController.grantCredit(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cred-1", response.getBody().getId());
    }

    @Test
    void getCreditById_found() {
        when(creditService.findById("cred-1")).thenReturn(Optional.of(personalCredit));
        when(creditService.toResponse(personalCredit)).thenReturn(personalResponse);

        ResponseEntity<CreditResponse> response = creditController.getCreditById("cred-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("cred-1", response.getBody().getId());
    }

    @Test
    void getCreditById_notFound() {
        when(creditService.findById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<CreditResponse> response = creditController.getCreditById("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAllCredits_returnsList() {
        List<Credit> credits = Arrays.asList(personalCredit, businessCredit);
        when(creditService.findAll()).thenReturn(credits);
        when(creditService.toResponseList(credits))
                .thenReturn(Arrays.asList(personalResponse, businessResponse));

        ResponseEntity<List<CreditResponse>> response = creditController.getAllCredits();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void updateCredit_found() {
        when(creditService.update("cred-1", new BigDecimal("0.15"), 30))
                .thenReturn(Optional.of(personalCredit));
        when(creditService.toResponse(personalCredit)).thenReturn(personalResponse);

        CreditUpdateRequest request = new CreditUpdateRequest();
        request.setInterestRate(new BigDecimal("0.15"));
        request.setTermMonths(30);
        ResponseEntity<CreditResponse> response = creditController.updateCredit("cred-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateCredit_notFound() {
        when(creditService.update("nonexistent", null, null)).thenReturn(Optional.empty());

        CreditUpdateRequest request = new CreditUpdateRequest();
        ResponseEntity<CreditResponse> response = creditController.updateCredit("nonexistent", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteCredit_found() {
        when(creditService.delete("cred-1")).thenReturn(true);

        ResponseEntity<Void> response = creditController.deleteCredit("cred-1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteCredit_notFound() {
        when(creditService.delete("nonexistent")).thenReturn(false);

        ResponseEntity<Void> response = creditController.deleteCredit("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
