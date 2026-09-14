package com.bank.mscredit.controller;

import com.bank.mscredit.dto.CreditResponse;
import com.bank.mscredit.dto.CreditUpdateRequest;
import com.bank.mscredit.dto.GrantCreditRequest;
import com.bank.mscredit.model.Credit;
import com.bank.mscredit.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestion de creditos.
 * Expone otorgamiento de creditos (personales y empresariales) y CRUD completo.
 */
@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    /**
     * Otorga un credito a un cliente.
     *
     * @param request datos del credito (customerId, creditType, amount, interestRate, termMonths)
     * @return el credito otorgado con codigo 201
     */
    @PostMapping
    public ResponseEntity<CreditResponse> grantCredit(@RequestBody GrantCreditRequest request) {
        Credit credit = creditService.grantCredit(
                request.getCustomerId(),
                request.getCreditType(),
                request.getAmount(),
                request.getInterestRate(),
                request.getTermMonths()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creditService.toResponse(credit));
    }

    /**
     * Obtiene un credito por su ID.
     *
     * @param id identificador del credito
     * @return el credito encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditResponse> getCreditById(@PathVariable String id) {
        return creditService.findById(id)
                .map(credit -> ResponseEntity.ok(creditService.toResponse(credit)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos los creditos registrados.
     *
     * @return lista de creditos
     */
    @GetMapping
    public ResponseEntity<List<CreditResponse>> getAllCredits() {
        return ResponseEntity.ok(creditService.toResponseList(creditService.findAll()));
    }

    /**
     * Actualiza las condiciones de un credito existente.
     *
     * @param id identificador del credito
     * @param request campos a actualizar (interestRate, termMonths)
     * @return el credito actualizado o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<CreditResponse> updateCredit(
            @PathVariable String id,
            @RequestBody CreditUpdateRequest request) {
        return creditService.update(id, request.getInterestRate(), request.getTermMonths())
                .map(credit -> ResponseEntity.ok(creditService.toResponse(credit)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un credito por su ID.
     *
     * @param id identificador del credito
     * @return 204 si se elimino, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredit(@PathVariable String id) {
        if (creditService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
