package com.bank.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuentas_ahorro")
@Getter @Setter @NoArgsConstructor
public class CuentaAhorro extends Cuenta {

    private double tasaInteres = 0.01; // 1% mensual

    public CuentaAhorro(String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        super(numeroCuenta, titular, cedula, direccion, telefono, saldo);
        this.saldoMinimo = 5.0; // Las cuentas de ahorro suelen pedir un mínimo para mantenerse activas
    }

    @Override
    public boolean retirar(double monto) {
        // Regla de negocio: No permite sobregiro (Saldo - monto >= saldoMinimo)
        if (monto > 0 && (saldo - monto) >= saldoMinimo) {
            saldo -= monto;
            return true;
        }
        return false;
    }

    @Override
    public void aplicarReglasFinDeMes() {
        // Aplicar el 1% de interés al saldo actual
        this.saldo += (this.saldo * tasaInteres);
    }
}