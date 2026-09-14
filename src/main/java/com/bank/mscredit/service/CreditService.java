package com.bank.mscredit.service;

import com.bank.mscredit.dto.CreditResponse;
import com.bank.mscredit.event.CreditEventProducer;
import com.bank.mscredit.model.Credit;
import com.bank.mscredit.model.CustomerView;
import com.bank.mscredit.repository.CreditRepository;
import com.bank.mscredit.repository.CustomerViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de gestion de creditos.
 * Expone CRUD completo y otorgamiento de creditos aplicando las reglas de
 * cardinalidad: cliente personal maximo 1 credito, cliente empresarial N.
 * La validacion del tipo de cliente usa la vista local customer_view
 * alimentada por eventos, sin llamadas REST a ms-customer.
 */
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final CustomerViewRepository customerViewRepository;
    private final CreditEventProducer creditEventProducer;

    /**
     * Otorga un credito a un cliente.
     * Valida que el monto sea positivo, que el tipo de credito sea consistente
     * con el tipo de cliente, y que un cliente personal no tenga ya un credito.
     *
     * @param customerId identificador del cliente
     * @param creditType tipo de credito (PERSONAL o BUSINESS)
     * @param amount monto otorgado (mayor a cero)
     * @param interestRate tasa de interes
     * @param termMonths plazo en meses
     * @return el credito otorgado
     * @throws IllegalArgumentException si el cliente no existe, el tipo es invalido
     *         o inconsistente, el monto no es positivo o el cliente personal ya
     *         tiene un credito
     */
    public Credit grantCredit(String customerId, String creditType, BigDecimal amount,
                              BigDecimal interestRate, Integer termMonths) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El monto del credito debe ser mayor a cero");
        }
        CustomerView customer = requireCustomer(customerId);
        if (Credit.TYPE_PERSONAL.equals(creditType)) {
            if (!Credit.TYPE_PERSONAL.equals(customer.getCustomerType())) {
                throw new IllegalArgumentException("Un credito personal requiere un cliente personal");
            }
            if (creditRepository.countByCustomerIdAndCreditType(customerId, Credit.TYPE_PERSONAL) > 0) {
                throw new IllegalArgumentException("El cliente personal ya tiene un credito");
            }
        } else if (Credit.TYPE_BUSINESS.equals(creditType)) {
            if (!Credit.TYPE_BUSINESS.equals(customer.getCustomerType())) {
                throw new IllegalArgumentException("Un credito empresarial requiere un cliente empresarial");
            }
        } else {
            throw new IllegalArgumentException("Tipo de credito invalido: " + creditType);
        }
        Credit saved = creditRepository.save(
                new Credit(customerId, creditType, amount, interestRate, termMonths));
        creditEventProducer.publishCreditGranted(saved);
        return saved;
    }

    /**
     * Busca un credito por su ID.
     *
     * @param id identificador del credito
     * @return optional con el credito encontrado
     */
    public Optional<Credit> findById(String id) {
        return creditRepository.findById(id);
    }

    /**
     * Lista todos los creditos registrados.
     *
     * @return lista de creditos
     */
    public List<Credit> findAll() {
        return creditRepository.findAll();
    }

    /**
     * Actualiza las condiciones de un credito existente.
     *
     * @param id identificador del credito
     * @param interestRate nueva tasa de interes (nullable, mantiene la actual)
     * @param termMonths nuevo plazo en meses (nullable, mantiene el actual)
     * @return el credito actualizado, o empty si no se encontro
     */
    public Optional<Credit> update(String id, BigDecimal interestRate, Integer termMonths) {
        return creditRepository.findById(id).map(credit -> {
            if (interestRate != null) {
                credit.setInterestRate(interestRate);
            }
            if (termMonths != null) {
                credit.setTermMonths(termMonths);
            }
            return creditRepository.save(credit);
        });
    }

    /**
     * Elimina un credito por su ID.
     *
     * @param id identificador del credito
     * @return true si se elimino, false si no existia
     */
    public boolean delete(String id) {
        if (creditRepository.existsById(id)) {
            creditRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Convierte una entidad Credit a su DTO de respuesta.
     *
     * @param credit entidad a convertir
     * @return DTO con los campos poblados
     */
    public CreditResponse toResponse(Credit credit) {
        CreditResponse response = new CreditResponse();
        response.setId(credit.getId());
        response.setCustomerId(credit.getCustomerId());
        response.setCreditType(credit.getCreditType());
        response.setAmount(credit.getAmount());
        response.setInterestRate(credit.getInterestRate());
        response.setTermMonths(credit.getTermMonths());
        response.setOutstandingBalance(credit.getOutstandingBalance());
        response.setStatus(credit.getStatus());
        return response;
    }

    /**
     * Convierte una lista de entidades Credit a DTOs de respuesta.
     *
     * @param credits lista de entidades
     * @return lista de DTOs
     */
    public List<CreditResponse> toResponseList(List<Credit> credits) {
        return credits.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CustomerView requireCustomer(String customerId) {
        return customerViewRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente no encontrado o no sincronizado: " + customerId));
    }
}
