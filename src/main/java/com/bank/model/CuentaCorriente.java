package com.bank.model; // Unificado con Cuenta y CuentaAhorro

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuentas_corriente")
@Getter @Setter @NoArgsConstructor
public class CuentaCorriente extends Cuenta {

    private double limiteSobregiro = 1000.0;

    public CuentaCorriente(String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        super(numeroCuenta, titular, cedula, direccion, telefono, saldo);
        // En cuenta corriente, el saldo mínimo suele ser 0 o incluso negativo
        this.saldoMinimo = -limiteSobregiro;
    }

    @Override
    public boolean retirar(double monto) {
        // Regla: Saldo disponible real + crédito permitido
        if (monto > 0 && (saldo + limiteSobregiro) >= monto) {
            saldo -= monto;
            return true;
        }
        return false;
    }

    @Override
    public void aplicarReglasFinDeMes() {
        // Comisión fija por mantenimiento (Business Rule típica de cuenta corriente)
        if (saldo > 10.0) {
            saldo -= 10.0;
        }
    }
}