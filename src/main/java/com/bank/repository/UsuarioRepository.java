package com.bank.repository;

import com.bank.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Login: Busca al usuario por su nombre para validar con BCrypt
    Optional<Usuario> findByUsername(String username);

    // Seguridad: Verifica si el nombre ya está tomado
    boolean existsByUsername(String username);
}