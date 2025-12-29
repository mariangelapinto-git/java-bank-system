package service;

import com.bank.util.ConexionBD;
import model.*;
import java.sql.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class BankService {

    //CODIGOS A UTILIZAR EN EL METODO TRANSFERENCIAS ENTRE BANCOS

    private static final Map<String, String> LISTA_BANCOS = new HashMap<>();
    static {
        LISTA_BANCOS.put("0102", "Banco de Venezuela");
        LISTA_BANCOS.put("0105", "Mercantil");
        LISTA_BANCOS.put("0108", "Provincial");
        LISTA_BANCOS.put("0134", "Banesco");
        LISTA_BANCOS.put("0172", "Bancamiga");
    }

    // PROCEDIMIENTO DEL CASE 1. ABRIR CUENTA NUEVA-------------------------

    // 1. Agrega el parámetro 'int usuarioId' al método
    public void crearCuenta(Cuenta cuenta, int usuarioId) {
        // 2. Agrega 'usuario_id' al final de la lista de columnas y un '?' extra en VALUES
        String sql = "INSERT INTO cuentas (id, numero_cuenta, titular, cedula, direccion, telefono, saldo, estado, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVO', ?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cuenta.getId());
            ps.setString(2, cuenta.getNumeroCuenta());
            ps.setString(3, cuenta.getTitular());
            ps.setString(4, cuenta.getCedula());
            ps.setString(5, cuenta.getDireccion());
            ps.setString(6, cuenta.getTelefono());
            ps.setDouble(7, cuenta.getSaldo());

            // 3. Setea el ID del usuario en la posición 8
            ps.setInt(8, usuarioId);

            ps.executeUpdate();
            System.out.println("Cuenta creada con éxito.");
        } catch (SQLException e) {
            System.out.println("Error al crear cuenta: " + e.getMessage());
        }
    }

    // PROCEDIMIENTO DEL CASE 2. LISTAR CUENTAS
    public void listarCuentas() {

        String sql = "SELECT * FROM cuentas WHERE estado = 'ACTIVO'";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n" + "=".repeat(85));
            System.out.println("                LISTADO DE CUENTAS BANCARIAS ACTIVAS");
            System.out.println("=".repeat(85));
            System.out.printf("%-5s | %-15s | %-15s | %-20s | %-10s%n",
                    "ID", "Cédula/RIF", "Nro Cuenta", "Titular", "Saldo");
            System.out.println("-".repeat(85));

            while (rs.next()) {
                int id = rs.getInt("id");
                String cedula = rs.getString("cedula"); // Nuevo dato
                String nro = rs.getString("numero_cuenta");
                String titular = rs.getString("titular");
                double saldo = rs.getDouble("saldo");

                System.out.printf("%-5d | %-15s | %-15s | %-20s | %-10.2f%n",
                        id, cedula, nro, titular, saldo);
            }
            System.out.println("=".repeat(85));

        } catch (SQLException e) {
            System.out.println("Error al listar cuentas: " + e.getMessage());
        }
    }

    //=====================================================================
    //========================METODO COMPLEMENTO===========================
    //=====================================================================

    public void listarTodasLasCuentas() {
        String sql = "SELECT id, numero_cuenta, titular, saldo, estado FROM cuentas";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- AUDITORÍA GENERAL DE CUENTAS ---");
            System.out.printf("%-5s | %-15s | %-20s | %-10s | %-10s%n", "ID", "Nro Cuenta", "Titular", "Saldo", "Estado");

            while (rs.next()) {
                System.out.printf("%-5d | %-15s | %-20s | %-10.2f | %-10s%n",
                        rs.getInt("id"), rs.getString("numero_cuenta"),
                        rs.getString("titular"), rs.getDouble("saldo"),
                        rs.getString("estado"));
            }
        } catch (SQLException e) {
            System.out.println("Error en auditoría: " + e.getMessage());
        }
    }


    //PROCEDIMIENTO DEL CASE 3. CERRAR CUENTAS---------------------------

    public void cerrarCuenta(int id) {
        // 1. Primero verificamos si tiene saldo cero
        String sqlCheck = "SELECT saldo FROM cuentas WHERE id = ?";
        String sqlUpdate = "UPDATE cuentas SET estado = 'CERRADO' WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {

            // Verificación de saldo
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, id);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    double saldoActual = rs.getDouble("saldo");
                    if (saldoActual > 0) {
                        System.err.println("No se puede cerrar: La cuenta aún tiene un saldo de $" + saldoActual);
                        return;
                    }
                }
            }


            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                psUpdate.setInt(1, id);
                int filas = psUpdate.executeUpdate();

                if (filas > 0) {
                    System.out.println("La cuenta ID " + id + " ha sido CERRADA exitosamente.");
                } else {
                    System.out.println("No se encontró la cuenta.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    //=======================================================================
    //==================METODO COMPLEMENTARIO================================
    //=======================================================================

    public void reactivarCuenta(int id) {
        String sql = "UPDATE cuentas SET estado = 'ACTIVO' WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("Cuenta ID " + id + " reactivada exitosamente.");
            } else {
                System.out.println("No se encontró la cuenta.");
            }
        } catch (SQLException e) {
            System.out.println("Error al reactivar: " + e.getMessage());
        }
    }


    //PROCEDIMIENTO DEL CASE 4. CONSULTAR SALDO ------------------------------

    public void consultarSaldo(int id) {
        if (!esCuentaActiva(id)) return;
        String sql = "SELECT titular, saldo FROM cuentas WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String titular = rs.getString("titular");
                double saldo = rs.getDouble("saldo");
                System.out.println("\n--- ESTADO DE CUENTA ---");
                System.out.println("Titular: " + titular);
                System.out.println("Saldo Actual: $" + saldo);
            } else {
                System.out.println("Error: No se encontró una cuenta con el ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar saldo: " + e.getMessage());
        }
    }

    //================================================================
    //=========================METODOS COMPLEMENTOS===================
    //==================================================================


    // METODO PARA ACTUALIZAR EL SALDO
    public boolean actualizarSaldo(int id, double nuevoSaldo) {
        String sql = "UPDATE cuentas SET saldo = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, nuevoSaldo);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar saldo: " + e.getMessage());
            return false;
        }
    }

    //PROCEDIMIENTO DEL CASE 5. DEPOSITO ----------------------------

    public void depositar(int cuentaId, double monto) {
        if (monto <= 0) {
            System.out.println("El monto debe ser mayor a cero.");
            return;
        }

        if (!esCuentaActiva(cuentaId)) {
            return;
        }
        // DOS SQLs: uno para actualizar el saldo y otro para el historial
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";
        String sqlInsert = "INSERT INTO transacciones (cuenta_id, tipo, monto) VALUES (?, 'DEPOSITO', ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                 PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {

                // 1. Actualizar Saldo
                psUpdate.setDouble(1, monto);
                psUpdate.setInt(2, cuentaId);
                int filasCuentas = psUpdate.executeUpdate();

                // 2. Registrar Transacción
                psInsert.setInt(1, cuentaId);
                psInsert.setDouble(2, monto);
                psInsert.executeUpdate();

                if (filasCuentas > 0) {
                    con.commit(); // Si todo salió bien, guardamos los cambios permanentemente
                    System.out.println("¡Depósito exitoso de $" + monto + "!");
                } else {
                    con.rollback(); // Si la cuenta no existe, se deshace todo
                    System.out.println("Error: No se encontró la cuenta con ID " + cuentaId);
                }

            } catch (SQLException e) {
                con.rollback(); // Si hay error en el SQL, se deshace todo
                System.out.println("Error en la transacción: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    //PROCEDIMIENTO DEL CASE 6 RETIROS-------------------------------

    public void retirar(int cuentaId, double monto) {
        if (monto <= 0) {
            System.out.println("El monto a retirar debe ser mayor a cero.");
            return;
        }

        if (!esCuentaActiva(cuentaId)) {
            return;
        }
        // Consultar el saldo actual
        String sqlCheck = "SELECT saldo FROM cuentas WHERE id = ?";
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";
        String sqlInsert = "INSERT INTO transacciones (cuenta_id, tipo, monto) VALUES (?, 'RETIRO', ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false); // Inicia transacción manual

            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck);
                 PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                 PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {

                // 1. Verificar si hay SALDO SUFICIENTE
                psCheck.setInt(1, cuentaId);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    double saldoActual = rs.getDouble("saldo");

                    if (saldoActual >= monto) {
                        // 2. Realizar el DESCUENTO
                        psUpdate.setDouble(1, monto);
                        psUpdate.setInt(2, cuentaId);
                        psUpdate.executeUpdate();

                        // 3. Registrar en el HISTORIAL
                        psInsert.setInt(1, cuentaId);
                        psInsert.setDouble(2, monto);
                        psInsert.executeUpdate();

                        con.commit(); // Se aplican los cambios si esta todo perfecto
                        System.out.println("¡Retiro exitoso! Has retirado: $" + monto);
                    } else {
                        System.out.println("Error: Saldo insuficiente. Saldo disponible: $" + saldoActual);
                        con.rollback();
                    }
                } else {
                    System.out.println("Error: No se encontró la cuenta con ID " + cuentaId);
                }

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error en el retiro: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    //=======================================================================
    //============================METODO COMPLEMENTO==========================
    //========================================================================

    public boolean validarReglasDeRetiro(int cuentaId, double montoARetirar) {
        String sql = "SELECT saldo, saldo_minimo, limite_diario FROM cuentas WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cuentaId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double saldoActual = rs.getDouble("saldo");
                double saldoMinimo = rs.getDouble("saldo_minimo");
                double limiteDiario = rs.getDouble("limite_diario");

                if (limiteDiario <= 0) {
                    System.err.println("Error: Esta cuenta no tiene asignado un límite de retiro diario.");
                    return false;
                }

                // REGLA 1: Saldo Mínimo Obligatorio
                if ((saldoActual - montoARetirar) < saldoMinimo) {
                    System.err.println("Operación rechazada: El saldo debe quedar al menos en $" + saldoMinimo);
                    return false;
                }

                // REGLA 2: Límite Diario
                double yaRetiradoHoy = obtenerSumaRetirosHoy(cuentaId);

                if ((yaRetiradoHoy + montoARetirar) > limiteDiario) {
                    double disponible = limiteDiario - yaRetiradoHoy;
                    System.err.println("Operación rechazada: Límite diario de $" + limiteDiario + " excedido.");
                    System.err.println("Usted ya retiró $" + yaRetiradoHoy + " hoy. Disponible restante: $" + Math.max(0, disponible));
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error validando reglas de negocio: " + e.getMessage());
        }
        return false;
    }


    private double obtenerSumaRetirosHoy(int cuentaId) {
        // COALESCE(SUM(monto), 0) significa: "Si la suma es nula, devuélveme 0"
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM transacciones " +
                "WHERE cuenta_id = ? AND tipo = 'RETIRO' " +
                "AND DATE(fecha_hora) = CURDATE()";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cuentaId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error en suma de retiros: " + e.getMessage());
        }
        return 0.0;
    }

    private boolean esCuentaActiva(int id) {
        String sql = "SELECT estado FROM cuentas WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String estado = rs.getString("estado");
                if (estado.equals("ACTIVO")) {
                    return true;
                } else {
                    System.out.println("Operación denegada: La cuenta está " + estado);
                }
            } else {
                System.out.println("Error: La cuenta con ID " + id + " no existe.");
            }
        } catch (SQLException e) {
            System.out.println("Error al validar estado: " + e.getMessage());
        }
        return false;
    }

    //PROCEDIMIENTO DEL CASE 7. TRANSFERENCIAS-------------------------


    public void transferir(int idOrigen, String numeroCuentaDestino, double monto) {
        if (monto <= 0) {
            System.out.println("El monto debe ser mayor a cero.");
            return;
        }

        if (!esCuentaActiva(idOrigen)) return;

        // 1. Lógica Interbancaria: Validar código del banco
        String codigoBanco = numeroCuentaDestino.substring(0, 4);
        boolean esMismoBanco = codigoBanco.equals("0102");
        double comision = esMismoBanco ? 0.0 : (monto * 0.003);
        double montoTotalARestar = monto + comision;

        // SQLs necesarias
        String sqlCheck = "SELECT saldo FROM cuentas WHERE id = ?";
        String sqlRestar = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";
        String sqlHistorial = "INSERT INTO transacciones (cuenta_id, tipo, monto) VALUES (?, ?, ?)";

        // NUEVAS SQLs para el destino
        String sqlBuscarDestino = "SELECT id FROM cuentas WHERE numero_cuenta = ? AND estado = 'ACTIVO'";
        String sqlSumarDestino = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false); // Iniciamos transacción

            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck);
                 PreparedStatement psRestar = con.prepareStatement(sqlRestar);
                 PreparedStatement psHistorial = con.prepareStatement(sqlHistorial);
                 PreparedStatement psBuscaDest = con.prepareStatement(sqlBuscarDestino);
                 PreparedStatement psSumarDest = con.prepareStatement(sqlSumarDestino)) {

                // 1. Verificar saldo de origen (incluyendo comisión)
                psCheck.setInt(1, idOrigen);
                ResultSet rs = psCheck.executeQuery();

                if (!rs.next() || rs.getDouble("saldo") < montoTotalARestar) {
                    System.out.println("Fallo: Saldo insuficiente o cuenta origen no existe.");
                    con.rollback();
                    return;
                }

                // 2. Ejecutar la resta en la cuenta de origen
                psRestar.setDouble(1, montoTotalARestar);
                psRestar.setInt(2, idOrigen);
                psRestar.executeUpdate();

                // 3. Registrar salida en historial del origen
                psHistorial.setInt(1, idOrigen);
                psHistorial.setString(2, "TRANSFERENCIA_SALIDA");
                psHistorial.setDouble(3, monto);
                psHistorial.executeUpdate();

                if (comision > 0) {
                    psHistorial.setInt(1, idOrigen);
                    psHistorial.setString(2, "COMISION_INTERBANCARIA");
                    psHistorial.setDouble(3, comision);
                    psHistorial.executeUpdate();
                }

                // 4. LÓGICA DE DEPÓSITO EN DESTINO (Si es cuenta interna)
                psBuscaDest.setString(1, numeroCuentaDestino);
                ResultSet rsDestino = psBuscaDest.executeQuery();

                if (rsDestino.next()) {
                    // La cuenta existe en nuestro banco, procedemos a sumar el dinero
                    int idDestinoReal = rsDestino.getInt("id");

                    psSumarDest.setDouble(1, monto);
                    psSumarDest.setInt(2, idDestinoReal);
                    psSumarDest.executeUpdate();

                    // Registrar entrada en historial del destino
                    psHistorial.setInt(1, idDestinoReal);
                    psHistorial.setString(2, "TRANSFERENCIA_ENTRADA");
                    psHistorial.setDouble(3, monto);
                    psHistorial.executeUpdate();

                    System.out.println("Transferencia interna exitosa. Saldo actualizado en cuenta destino.");
                } else {
                    // No está en nuestra DB, se asume enviada a otro banco
                    String nombreBanco = LISTA_BANCOS.getOrDefault(codigoBanco, "Banco Externo");
                    System.out.println("Fondos enviados con éxito a banca externa: " + nombreBanco);
                }

                con.commit(); // Todo salió bien, aplicamos cambios

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error crítico en la transferencia: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    // =================================================================
    // NUEVO: MÓDULO DE PRÉSTAMOS (BASADO EN PROMEDIO)
    // =================================================================
    public void evaluarYOfrecerPrestamo(int cuentaId) {
        // Obtenemos el promedio de depósitos de los últimos 30 días
        String sql = "SELECT AVG(monto) FROM transacciones WHERE cuenta_id = ? AND tipo = 'DEPOSITO'";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cuentaId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double promedio = rs.getDouble(1);
                System.out.println("\n--- EVALUACIÓN DE CRÉDITO ---");
                if (promedio >= 500) {
                    double montoPrestamo = promedio * 3; // Ofrece 3 veces su promedio
                    System.out.println("¡Estado: APROBADO!");
                    System.out.println("Basado en su promedio mensual de $" + String.format("%.2f", promedio));
                    System.out.println("Podemos ofrecerle un préstamo de hasta: $" + String.format("%.2f", montoPrestamo));
                } else {
                    System.out.println("Estado: RECHAZADO");
                    System.out.println("Se requiere un promedio de depósitos mayor a $500. Su promedio: $" + String.format("%.2f", promedio));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al evaluar préstamo: " + e.getMessage());
        }
    }

    //PROCEDIMIENTO DEL CASE 8 EXTRACTO BANCARIO

    public void verExtracto(int cuentaId) {
        String sqlCuenta = "SELECT titular, saldo FROM cuentas WHERE id = ?";
        String sqlTrans = "SELECT tipo, monto, fecha_hora FROM transacciones WHERE cuenta_id = ? ORDER BY fecha_hora DESC";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement psC = con.prepareStatement(sqlCuenta);
             PreparedStatement psT = con.prepareStatement(sqlTrans)) {

            // 1. Obtener datos del titular
            psC.setInt(1, cuentaId);
            ResultSet rsC = psC.executeQuery();

            if (rsC.next()) {
                System.out.println("\n========================================");
                System.out.println("       EXTRACTO BANCARIO");
                System.out.println("========================================");
                System.out.println("Titular: " + rsC.getString("titular"));
                System.out.println("Saldo Actual: $" + rsC.getDouble("saldo"));
                System.out.println("----------------------------------------");

                // 2. Obtener movimientos
                psT.setInt(1, cuentaId);
                ResultSet rsT = psT.executeQuery();

                System.out.printf("%-20s | %-10s | %-15s%n", "Fecha", "Monto", "Operación");
                System.out.println("----------------------------------------");

                boolean tieneMovimientos = false;
                while (rsT.next()) {
                    tieneMovimientos = true;
                    System.out.printf("%-20s | %-10.2f | %-15s%n",
                            rsT.getTimestamp("fecha_hora"),
                            rsT.getDouble("monto"),
                            rsT.getString("tipo"));
                }

                if (!tieneMovimientos) {
                    System.out.println("No hay movimientos registrados para esta cuenta.");
                }
                System.out.println("========================================\n");

            } else {
                System.out.println("Error: No se encontró la cuenta con ID " + cuentaId);
            }

        } catch (SQLException e) {
            System.out.println("Error al generar el extracto: " + e.getMessage());
        }
    }

    //PROCEDIMIENTO DEL CASE 10. GENERAR PDF


    public void generarReportePDF(int cuentaId) {

        String nombreArchivo = "Estado_Cuenta_" + cuentaId + ".pdf";
        String sqlCuenta = "SELECT * FROM cuentas WHERE id = ?";
        String sqlTrans = "SELECT tipo, monto, fecha_hora FROM transacciones WHERE cuenta_id = ? ORDER BY fecha_hora DESC LIMIT 10";

        // 1. Crear el documento de iText 5
        Document documento = new Document(PageSize.A4);

        try (Connection con = ConexionBD.obtenerConexion()) {
            PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));

            // --- AGREGA PIE DE PÁGINA CON FECHA Y HORA ---
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    String fechaHora = "Impreso el: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                            new Phrase(fechaHora, new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC)),
                            document.right(), document.bottom() - 15, 0);
                }
            });

            documento.open();

            // --- 2. CONSULTAR DATOS DE LA CUENTA ---
            try (PreparedStatement psC = con.prepareStatement(sqlCuenta)) {
                psC.setInt(1, cuentaId);
                ResultSet rsC = psC.executeQuery();

                if (rsC.next()) {
                    Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                    documento.add(new Paragraph("JAVABANK SYSTEM - REPORTE OFICIAL", fontTitulo));
                    documento.add(new Paragraph("--------------------------------------------------------------------------"));
                    documento.add(new Paragraph("Titular: " + rsC.getString("titular")));
                    documento.add(new Paragraph("Cédula/RIF: " + rsC.getString("cedula")));
                    documento.add(new Paragraph("Saldo Actual: $" + String.format("%.2f", rsC.getDouble("saldo"))));
                    documento.add(new Paragraph("\n"));

                    // --- 3. TABLA DE MOVIMIENTOS ---
                    PdfPTable tabla = new PdfPTable(3); // 3 columnas
                    tabla.setWidthPercentage(100);

                    // Cabeceras de la tabla
                    tabla.addCell(new PdfPCell(new Phrase("Fecha y Hora", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
                    tabla.addCell(new PdfPCell(new Phrase("Monto", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
                    tabla.addCell(new PdfPCell(new Phrase("Operación", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));

                    try (PreparedStatement psT = con.prepareStatement(sqlTrans)) {
                        psT.setInt(1, cuentaId);
                        ResultSet rsT = psT.executeQuery();
                        while (rsT.next()) {
                            tabla.addCell(rsT.getTimestamp("fecha_hora").toString());
                            tabla.addCell("$" + String.format("%.2f", rsT.getDouble("monto")));
                            tabla.addCell(rsT.getString("tipo")); // Cambiado a tipo_transaccion según tu DB
                        }
                    }

                    documento.add(tabla);
                    System.out.println("PDF '" + nombreArchivo + "' generado con éxito.");

                } else {
                    documento.add(new Paragraph("No se encontró información para la cuenta ID: " + cuentaId));
                }
            }

        } catch (Exception e) {
            System.err.println("Error crítico al generar PDF: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    //PROCEDIMIENTO PARA EL CASE 11. INTERESES--------------------------------------

    public void ejecutarInteresesBatch() {
        // 3% de interés para cuentas ACTIVAS
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo + (saldo * 0.03) WHERE estado = 'ACTIVO'";
        String sqlTrans = "INSERT INTO transacciones (cuenta_id, tipo, monto) " +
                "SELECT id, 'INTERES_GANADO', (saldo * 0.03) FROM cuentas WHERE estado = 'ACTIVO'";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false); // Transacción para seguridad total

            try (PreparedStatement psUpd = con.prepareStatement(sqlUpdate);
                 PreparedStatement psTr = con.prepareStatement(sqlTrans)) {

                psUpd.executeUpdate();
                psTr.executeUpdate();

                con.commit();
                System.out.println("¡Éxito! Intereses del 3% aplicados a todas las cuentas activas.");
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error: Se canceló el proceso de intereses.");
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error en DB: " + e.getMessage());
        }
    }

    //PROCEDIMIENTO DEL CASE 12. DISPONIBILIDAD Y LIMITE DE RETIRO

    public void mostrarDetallesDeCuenta(int cuentaId) {
        // 1. Consulta para los datos base de la cuenta
        String sqlCuenta = "SELECT titular, saldo, saldo_minimo, limite_diario FROM cuentas WHERE id = ?";

        // 2. Consulta para lo retirado hoy (tu lógica original integrada)
        String sqlRetirosHoy = "SELECT SUM(monto) FROM transacciones " +
                "WHERE cuenta_id = ? AND tipo = 'RETIRO' " +
                "AND DATE(fecha_hora) = CURDATE()";

        try (Connection con = ConexionBD.obtenerConexion()) {

            double yaRetiradoHoy = 0;

            // Ejecutamos primero el cálculo de retiros del día
            try (PreparedStatement psR = con.prepareStatement(sqlRetirosHoy)) {
                psR.setInt(1, cuentaId);
                ResultSet rsR = psR.executeQuery();
                if (rsR.next()) {
                    yaRetiradoHoy = rsR.getDouble(1);
                }
            }

            // Ahora consultamos los datos de la cuenta y los mostramos
            try (PreparedStatement psC = con.prepareStatement(sqlCuenta)) {
                psC.setInt(1, cuentaId);
                ResultSet rsC = psC.executeQuery();

                if (rsC.next()) {
                    double saldo = rsC.getDouble("saldo");
                    double minimo = rsC.getDouble("saldo_minimo");
                    double limiteDiario = rsC.getDouble("limite_diario");
                    double disponibleHoy = limiteDiario - yaRetiradoHoy;

                    System.out.println("\n-------------------------------------------");
                    System.out.println("       DETALLES DE SEGURIDAD Y SALDO");
                    System.out.println("-------------------------------------------");
                    System.out.println("Titular:          " + rsC.getString("titular"));
                    System.out.println("Saldo Contable:   $" + String.format("%.2f", saldo));
                    System.out.println("Monto Retirado Hoy: $" + String.format("%.2f", yaRetiradoHoy));
                    System.out.println("-------------------------------------------");
                    System.out.println("Saldo Mínimo Requerido:  $" + String.format("%.2f", minimo));
                    System.out.println("Límite Diario Total:     $" + String.format("%.2f", limiteDiario));
                    System.out.println("DISPONIBLE PARA RETIRO:  $" + String.format("%.2f", Math.max(0, disponibleHoy)));
                    System.out.println("-------------------------------------------\n");
                } else {
                    System.out.println("La cuenta con ID " + cuentaId + " no existe.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al mostrar detalles: " + e.getMessage());
        }
    }

    //=================================================================
    //=================================================================
    //--------------------APARTADO PARA USUARIO EN SQL ----------------
    //-----------------------------------------------------------------

    // NOTA IMPORTANTE----> YA EXISTE (PARA CREAR el usuario una sola vez)

    public boolean registrarUsuario(String user, String pass, String rol) {
        String sql = "INSERT INTO usuarios (username, password, rol) VALUES (?, ?, ?)";
        String passwordHaseada = BCrypt.hashpw(pass, BCrypt.gensalt());

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, passwordHaseada);
            ps.setString(3, rol);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar: " + e.getMessage());
            return false;
        }
    }

    //==============================LOGIN DEL USUARIO (ADMIN)=========================

    public Usuario login(String user, String pass) {
        String sql = "SELECT id, username, password, rol, intentos_fallidos, bloqueado FROM usuarios WHERE username = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Aquí ya extraes el id correctamente como entero
                int id = rs.getInt("id");
                boolean estaBloqueado = rs.getBoolean("bloqueado");
                int intentos = rs.getInt("intentos_fallidos");
                String hashAlmacenado = rs.getString("password");

                if (estaBloqueado) {
                    System.err.println("Acceso denegado: El usuario '" + user + "' está bloqueado.");
                    return null;
                }

                if (BCrypt.checkpw(pass, hashAlmacenado)) {
                    if (intentos > 0) {
                        ejecutarUpdateSeguridad(id, 0, false);
                    }

                    // --- ARREGLO AQUÍ ---
                    // Usamos la variable 'id' que es int, NO 'rs.getInt("password")'
                    return new Usuario(id, rs.getString("username"), rs.getString("rol"));
                    // ---------------------

                } else {
                    manejarFalloAutenticacion(id, intentos);
                }
            } else {
                System.err.println("El usuario no existe.");
            }
        } catch (SQLException e) {
            System.err.println("Error en base de datos durante login: " + e.getMessage());
        }
        return null;
    }

    //==========================OBTENER TOTAL DE USUARIOS SI NO ENCUENTRA CREA UNO======

    public int obtenerTotalUsuarios() {
        String sql = "SELECT COUNT(*) AS total FROM usuarios";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al contar usuarios: " + e.getMessage());
        }
        return 0; // Si hay error o está vacía, devuelve 0
    }

    //=================================================================================
    //==========================SEGURIDAD PARA BASE DE DATO============================
    //=================================================================================


    //*******************METODO #1

    private void manejarFalloAutenticacion(int userId, int intentosActuales) throws SQLException {
        int nuevosIntentos = intentosActuales + 1;
        boolean bloquear = nuevosIntentos >= 3;

        ejecutarUpdateSeguridad(userId, nuevosIntentos, bloquear);

        if (bloquear) {
            System.err.println("¡ALERTA! El usuario ha sido bloqueado tras 3 intentos fallidos.");
        } else {
            System.err.println("Clave incorrecta. Intentos: " + nuevosIntentos + "/3");
        }
    }

    //*******************METODO #2

    private void ejecutarUpdateSeguridad(int userId, int intentos, boolean bloquear) throws SQLException {
        String sqlUpdate = "UPDATE usuarios SET intentos_fallidos = ?, bloqueado = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
            ps.setInt(1, intentos);
            ps.setBoolean(2, bloquear);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

}