package com.yassmine.administration.exception;

/**
 * Levée lorsqu'un utilisateur (admin) tente de se connecter alors que
 * son compte est marqué comme bloqué (temporairement ou indéfiniment).
 */
public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String message) {
        super(message);
    }
}