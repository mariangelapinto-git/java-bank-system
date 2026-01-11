package com.bank.repository;

import com.bank.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // Busca una cuenta específica por su número único
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByEstado(String estado);

    // Usar una consulta personalizada ayuda a Hibernate a identificar las entidades correctamente
    @Query("SELECT c FROM Cuenta c WHERE c.id = :id")
    Optional<Cuenta> buscarPorIdSeguro(Long id);

    // --- INTERESES SEGÚN REGLAS DE NEGOCIO ---

    @Transactional
    @Modifying
    @Query("UPDATE Cuenta c SET c.saldo = c.saldo + (c.saldo * 0.03) WHERE c.estado = 'ACTIVO'")
    void aplicarInteresesGlobales();
}