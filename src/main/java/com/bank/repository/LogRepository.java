package com.bank.repository;

import com.bank.model.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogSistema, Long> {

    // Spring Data JPA crea automáticamente la consulta para traer los últimos 15 logs.
    List<LogSistema> findTop15ByOrderByFechaHoraDesc();

    List<LogSistema> findByUsuarioId(Long usuarioId);
}