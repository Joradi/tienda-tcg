package com.tiendatcg.importation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImportationCardNotFoundException
        extends RuntimeException {

    public ImportationCardNotFoundException(Long cardId) {
        super("Carta no encontrada con id " + cardId);
    }
}