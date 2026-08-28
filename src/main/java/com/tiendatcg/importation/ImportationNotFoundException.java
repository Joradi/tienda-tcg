package com.tiendatcg.importation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImportationNotFoundException extends RuntimeException {

    public ImportationNotFoundException(Long id) {
        super("No se encontró la importación con id " + id);
    }
}