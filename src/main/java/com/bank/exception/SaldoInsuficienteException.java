package com.bank.exception;

/**
 * Se lanza cuando el saldo disponible (incluyendo sobregiros si aplican)
 * no es suficiente para cubrir la operación o el saldo mínimo.
 */
public class SaldoInsuficienteException extends BankException {
    public SaldoInsuficienteException(String message) {
        super(message);
    }
}
