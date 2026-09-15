package com.bank.mscredit.repository;

import com.bank.mscredit.model.Movement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repositorio para la entidad Movement en MongoDB.
 * No se permite @Query ni consultas dinamicas segun las reglas del proyecto.
 */
public interface MovementRepository extends MongoRepository<Movement, String> {

    /**
     * Lista los movimientos de un credito del mas reciente al mas antiguo.
     *
     * @param creditId identificador del credito
     * @return lista de movimientos ordenada por fecha descendente
     */
    List<Movement> findByCreditIdOrderByOccurredAtDesc(String creditId);
}
