package com.bank.exception;

/**
 * Excepción disparada cuando se intenta operar sobre una cuenta con estado BLOQUEADO.
 * Útil para el sistema de alertas de seguridad y fraudes.
 */
public class CuentaBloqueadaException extends BankException {

    public CuentaBloqueadaException(String message) {
        super(message);
    }
}