package com.bank.service;

import com.bank.model.*;
import com.bank.repository.*;
import com.bank.exception.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BankService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Necesario para el login y logs

    @Autowired
    private LogRepository logRepository; // Para la función 9 de Auditoría

    // 1. LOGIN CON BCRYPT (Función Crítica)
    public Optional<Usuario> login(String username, String password) {
        return usuarioRepository.findByUsername(username)
                .filter(user -> BCrypt.checkpw(password, user.getPassword()));
    }

    // 2. OBTENER LOGS PARA AUDITORÍA
    public List<String> obtenerLogsRecientes() {
        return logRepository.findTop15ByOrderByFechaHoraDesc().stream()
                .map(log -> String.format("[%s] %s: %s - %s",
                        log.getFechaHora(), log.getUsuario().getUsername(),
                        log.getAccion(), log.getDetalles()))
                .collect(Collectors.toList());
    }

    // 3. RETIRO CON REGLAS DE NEGOCIO
    @Transactional
    public void retirar(Long cuentaId, double monto, Long usuarioId) {
        if (monto <= 0) throw new BankException("El monto de retiro debe ser positivo.");

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new BankException("La cuenta solicitada no existe."));

        validarEstadoHabilitado(cuenta);

        // Regla de Saldo Mínimo
        if ((cuenta.getSaldo() - monto) < cuenta.getSaldoMinimo()) {
            registrarLog(usuarioId, "FRAUDE_INTENTO", "Intento de retiro por debajo del mínimo en cuenta: " + cuentaId);
            throw new SaldoInsuficienteException("Fondos insuficientes para mantener el saldo mínimo.");
        }

        // Regla de Límite Diario
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        Double yaRetiradoHoy = transaccionRepository.sumByCuentaIdAndTipoAndFechaHoraAfter(cuentaId, "RETIRO", inicioDia);
        if (yaRetiradoHoy == null) yaRetiradoHoy = 0.0;

        if ((yaRetiradoHoy + monto) > cuenta.getLimiteDiario()) {
            throw new BankException("Límite diario excedido.");
        }

        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuentaRepository.save(cuenta);
        registrarTransaccion(cuenta, "RETIRO", monto);
    }

    // 4. EVALUAR PRÉSTAMO (Algoritmo de Riesgo)
    public String evaluarPrestamo(Long cuentaId) {
        Double promedio = transaccionRepository.obtenerPromedioDepositos(cuentaId);
        if (promedio == null || promedio < 500) {
            return "RECHAZADO: El promedio de depósitos mensual ($" + (promedio != null ? promedio : 0) + ") es insuficiente.";
        }
        double aprobado = promedio * 3;
        return "¡APROBADO!\nMonto sugerido: $" + String.format("%.2f", aprobado) + "\nTasa: 12% E.A.";
    }

    // 5. MÉTODOS DE APOYO (Transacciones y Logs)
    @Transactional
    public void registrarLog(Long usuarioId, String accion, String detalles) {
        Usuario user = usuarioRepository.findById(usuarioId).orElse(null);
        LogSistema log = new LogSistema();
        log.setUsuario(user);
        log.setAccion(accion);
        log.setDetalles(detalles);
        log.setFechaHora(LocalDateTime.now());
        logRepository.save(log);
    }

    private void registrarTransaccion(Cuenta cuenta, String tipo, double monto) {
        Transaccion t = new Transaccion();
        t.setCuenta(cuenta);
        t.setTipo(tipo);
        t.setMonto(monto);
        t.setFechaHora(LocalDateTime.now());
        transaccionRepository.save(t);
    }

    private void validarEstadoHabilitado(Cuenta cuenta) {
        if ("BLOQUEADO".equals(cuenta.getEstado())) {
            throw new CuentaBloqueadaException("Cuenta bloqueada.");
        }
        if ("CERRADO".equals(cuenta.getEstado())) {
            throw new BankException("Cuenta cerrada.");
        }
    }

    @Transactional
    public void crearCuenta(Cuenta cuenta, Long usuarioId) {
        // Buscamos al usuario dueño de la cuenta
        Usuario dueño = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BankException("Usuario no encontrado"));

        // Asignamos el usuario a la cuenta antes de guardar
        cuenta.setUsuario(dueño);

        cuentaRepository.save(cuenta);
    }

    public List<Cuenta> obtenerTodasLasCuentas() {
        return cuentaRepository.findAll();
    }

    @Transactional
    public void depositar(Long cuentaId, double monto) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow(() -> new BankException("No existe la cuenta"));
        validarEstadoHabilitado(cuenta);
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        cuentaRepository.save(cuenta);
        registrarTransaccion(cuenta, "DEPOSITO", monto);
    }

    @Transactional
    public void ejecutarInteresesBatch() {
        List<Cuenta> activas = cuentaRepository.findByEstado("ACTIVO");
        for (Cuenta c : activas) {
            c.aplicarReglasFinDeMes();
            cuentaRepository.save(c);
        }
    }

    @Transactional
    public void cambiarEstadoCuenta(Long id, String nuevoEstado) {
        Cuenta c = cuentaRepository.findById(id).orElseThrow(() -> new BankException("No existe"));
        c.setEstado(nuevoEstado);
        cuentaRepository.save(c);
    }

    public Optional<Cuenta> consultarCuenta(Long idCons) {
        return cuentaRepository.findById(idCons);
    }

    // 6. TRANSFERENCIA ATÓMICA (Implementación corregida)
    @Transactional
    public void transferir(Long idOri, String numCuentaDestino, double montoTrans, Long usuarioAutorizaId) {
        if (montoTrans <= 0) throw new BankException("El monto debe ser positivo.");

        // Origen
        Cuenta origen = cuentaRepository.findById(idOri)
                .orElseThrow(() -> new BankException("Cuenta origen no encontrada."));

        // Destino (Buscamos por número de cuenta String como pide tu Dashboard)
        Cuenta destino = cuentaRepository.findByNumeroCuenta(numCuentaDestino)
                .orElseThrow(() -> new BankException("La cuenta destino " + numCuentaDestino + " no existe."));

        if (origen.getId().equals(destino.getId())) {
            throw new BankException("No se puede transferir a la misma cuenta.");
        }

        validarEstadoHabilitado(origen);
        validarEstadoHabilitado(destino);

        // Validación de saldo
        if (origen.getSaldo() < montoTrans) {
            throw new SaldoInsuficienteException("Saldo insuficiente para transferir.");
        }

        // Ejecución
        origen.setSaldo(origen.getSaldo() - montoTrans);
        destino.setSaldo(destino.getSaldo() + montoTrans);

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        // Registro de transacciones para ambos
        registrarTransaccion(origen, "TRANSFERENCIA_SALIDA", montoTrans);
        registrarTransaccion(destino, "TRANSFERENCIA_ENTRADA", montoTrans);

        // Auditoría detallada [cite: 2025-12-27]
        registrarLog(usuarioAutorizaId, "TRANSFERENCIA",
                String.format("Transferencia de $%s desde ID:%s hacia %s", montoTrans, idOri, numCuentaDestino));
    }

    // 7. REGISTRO DE SEGURIDAD (Para VentanaLogin)
    @Transactional
    public void registrarLogSeguridad(String username, String accion, String detalles) {
        // Buscamos si el usuario existe para vincular el log, si no, lo guardamos como anónimo
        Usuario user = usuarioRepository.findByUsername(username).orElse(null);

        LogSistema log = new LogSistema();
        log.setUsuario(user);
        log.setAccion(accion);
        log.setDetalles("Usuario [" + username + "]: " + detalles);
        log.setFechaHora(LocalDateTime.now());

        logRepository.save(log);

        // Si es un fallo de seguridad, lo imprimimos en consola del servidor (simulación de alerta)
        System.err.println("!!! SECURITY ALERT: " + accion + " - " + detalles);
    }

    // Método extra para obtener saldo rápido
    public double obtenerSaldo(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .map(Cuenta::getSaldo)
                .orElseThrow(() -> new BankException("Cuenta no encontrada"));
    }
}