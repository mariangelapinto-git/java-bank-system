package model;

public class CuentaAhorro extends Cuenta {
    private double tasaInteres = 0.01; // 1% mensual por ejemplo

    public CuentaAhorro(int id, String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        super(id, numeroCuenta, titular, cedula, direccion, telefono, saldo);
    }

    @Override
    public boolean retirar(double monto) {
        if (monto > 0 && monto <= saldo) {
            saldo -= monto;
            return true;
        }
        return false; // No permite sobregiro
    }

    @Override
    public void aplicarReglasFinDeMes() {
        saldo += (saldo * tasaInteres);
    }
}
