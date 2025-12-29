package model;

// 1. La hacemos abstracta
public abstract class Cuenta {
    protected int id;
    protected String numeroCuenta;
    protected String titular;
    protected String cedula;
    protected String direccion;
    protected String telefono;
    protected double saldo;

    public Cuenta(int id, String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        this.id = id;
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.cedula = cedula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.saldo = saldo;
    }

    // 2. Métodos comunes que no cambian
    public void depositar(double monto) {
        if (monto > 0) {
            this.saldo += monto;
        }
    }

    // 3. Métodos abstractos: cada hija los implementará diferente
    public abstract boolean retirar(double monto);
    public abstract void aplicarReglasFinDeMes();

    public String getCedula() {
        return cedula;
    }
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }
}