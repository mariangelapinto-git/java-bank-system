package model;

public class CuentaCorriente extends Cuenta {
    private double limiteSobregiro = 1000.0; // Puede quedar en -1000

    public CuentaCorriente(int id, String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        super(id, numeroCuenta, titular, cedula, direccion, telefono, saldo);
    }

    @Override
    public boolean retirar(double monto) {
        if (monto > 0 && monto <= (saldo + limiteSobregiro)) {
            saldo -= monto;
            return true;
        }
        return false;
    }

    @Override
    public void aplicarReglasFinDeMes() {
        // Podrías cobrar una comisión por mantenimiento aquí
        saldo -= 10.0;
    }
}