package com.bank.mscredit.repository;

import com.bank.mscredit.model.Credit;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio para la entidad Credit en MongoDB.
 * No se permite @Query ni consultas dinamicas segun las reglas del proyecto.
 */
public interface CreditRepository extends MongoRepository<Credit, String> {

    /**
     * Cuenta los creditos de un tipo que tiene un cliente.
     * Se usa para validar la regla de un solo credito por cliente personal.
     *
     * @param customerId identificador del cliente
     * @param creditType tipo de credito (PERSONAL o BUSINESS)
     * @return numero de creditos del tipo indicado que tiene el cliente
     */
    long countByCustomerIdAndCreditType(String customerId, String creditType);
}
