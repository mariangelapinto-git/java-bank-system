package com.bank.repository;

import com.bank.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // Lógica para el límite diario
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t " +
            "WHERE t.cuenta.id = :cuentaId AND t.tipo = :tipo AND t.fechaHora >= :fecha")
    double sumByCuentaIdAndTipoAndFechaHoraAfter(@Param("cuentaId") Long cuentaId,
                                                 @Param("tipo") String tipo,
                                                 @Param("fecha") LocalDateTime fecha);

    // Lógica para el módulo de préstamos
    @Query("SELECT AVG(t.monto) FROM Transaccion t WHERE t.cuenta.id = :cuentaId AND t.tipo = 'DEPOSITO'")
    Double obtenerPromedioDepositos(@Param("cuentaId") Long cuentaId);

    // Para ver el historial de movimientos
    List<Transaccion> findByCuentaIdOrderByFechaHoraDesc(Long cuentaId);
}