package model;

public class Cuenta {
    private int id;
    private String numeroCuenta; 
    private String titular;
    private String cedula;    // Nuevo
    private String direccion; // Nuevo
    private String telefono;
    private double saldo;

    public Cuenta(int id, String numeroCuenta,String titular, String cedula, String direccion, String telefono, double saldo) {
        this.id = id;
        this.numeroCuenta=numeroCuenta;
        this.titular = titular;
        this.cedula = cedula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.saldo=saldo;
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

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "Cuenta{" +
                "id=" + id +
                ", numeroCuenta='" + numeroCuenta + '\'' +
                ", titular='" + titular + '\'' +
                ", saldo=" + saldo +
                '}';
    }
}