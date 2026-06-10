package com.yassmine.administration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    // Constructeur de base sans message
    public UnauthorizedException() {
        super("Accès non autorisé : authentification requise ou jeton invalide.");
    }

    // Constructeur personnalisé pour spécifier la raison précise
    public UnauthorizedException(String message) {
        super(message);
    }
}