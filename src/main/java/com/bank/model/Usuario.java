package com.bank.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor // Constructor vacío obligatorio para JPA
@AllArgsConstructor // Constructor con todos los campos
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Cambiamos a Long por estándar de Spring

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String rol;

    @Column(name = "intentos_fallidos")
    private int intentosFallidos;

    private boolean bloqueado;

    public Usuario(Long id, String username, String rol) {
        this.id = id;
        this.username = username;
        this.rol = rol;
    }
}