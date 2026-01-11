package com.bank.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuentas_nomina")
@Getter @Setter @NoArgsConstructor
public class CuentaNomina extends Cuenta {

    public CuentaNomina(String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        super(numeroCuenta, titular, cedula, direccion, telefono, saldo);
        this.saldoMinimo = 0.0; // En nómina no suele haber mínimo
    }

    @Override
    public boolean retirar(double monto) {
        // Regla: Solo saldo disponible, sin sobregiros
        if (monto > 0 && monto <= saldo) {
            saldo -= monto;
            return true;
        }
        return false;
    }

    @Override
    public void aplicarReglasFinDeMes() {
        System.out.println("Cuenta de Nómina: Exenta de comisiones mensuales.");
    }
}
