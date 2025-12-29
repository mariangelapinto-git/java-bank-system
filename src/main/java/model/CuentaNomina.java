package model;

public class CuentaNomina extends Cuenta {
    public CuentaNomina(int id, String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        super(id, numeroCuenta, titular, cedula, direccion, telefono, saldo);
    }

    @Override
    public boolean retirar(double monto) {
        if (monto > 0 && monto <= saldo) {
            saldo -= monto;
            return true;
        }
        return false;
    }

    @Override
    public void aplicarReglasFinDeMes() {
        // No hace nada, es gratis por ser nómina
    }
}
