package com.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuentas")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public abstract class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true, nullable = false)
    protected String numeroCuenta;

    protected String titular;

    @Column(nullable = false)
    protected String cedula;

    protected String direccion;
    protected String telefono;
    protected double saldo;

    protected String estado = "ACTIVO";

    @Column(name = "saldo_minimo")
    protected double saldoMinimo = 0.0;

    @Column(name = "limite_diario")
    protected double limiteDiario = 1000.0;

    // --- RELACIÓN CON USUARIO  ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Cuenta(String numeroCuenta, String titular, String cedula, String direccion, String telefono, double saldo) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.cedula = cedula;
        this.direccion = direccion;
        this.telefono = telefono;
        this.saldo = saldo;
    }

    // Lógica de negocio base
    public void depositar(double monto) {
        if (monto > 0) {
            this.saldo += monto;
        }
    }

    public abstract boolean retirar(double monto);
    public abstract void aplicarReglasFinDeMes();
}