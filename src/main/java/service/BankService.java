package service;

import com.bank.util.ConexionBD;
import com.itextpdf.layout.element.Text;
import model.*;
import java.sql.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import java.sql.Timestamp;

public class BankService {

    // 1. LISTAR CUENTAS
    public void listarCuentas() {
        // Seguimos filtrando solo las cuentas ACTIVAS
        String sql = "SELECT * FROM cuentas WHERE estado = 'ACTIVO'";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n" + "=".repeat(85));
            System.out.println("                LISTADO DE CUENTAS BANCARIAS ACTIVAS");
            System.out.println("=".repeat(85));
            // Añadimos la columna Cédula/RIF
            System.out.printf("%-5s | %-15s | %-15s | %-20s | %-10s%n",
                    "ID", "Cédula/RIF", "Nro Cuenta", "Titular", "Saldo");
            System.out.println("-".repeat(85));

            while (rs.next()) {
                int id = rs.getInt("id");
                String cedula = rs.getString("cedula"); // Nuevo dato
                String nro = rs.getString("numero_cuenta");
                String titular = rs.getString("titular");
                double saldo = rs.getDouble("saldo");

                // Imprimimos la fila con el nuevo campo
                System.out.printf("%-5d | %-15s | %-15s | %-20s | %-10.2f%n",
                        id, cedula, nro, titular, saldo);
            }
            System.out.println("=".repeat(85));

        } catch (SQLException e) {
            System.out.println("Error al listar cuentas: " + e.getMessage());
        }
    }

    //================================================================
    //---------------GENERADOR DE PDF--------------------------------

    public void generarReportePDF(int cuentaId) {
        if (!esCuentaActiva(cuentaId)) return;

        String nombreArchivo = "Estado_Cuenta_" + cuentaId + ".pdf";
        String sqlCuenta = "SELECT * FROM cuentas WHERE id = ?";
        String sqlTrans = "SELECT tipo, monto, fecha_hora FROM transacciones WHERE cuenta_id = ? ORDER BY fecha_hora DESC";

        try (Connection con = ConexionBD.obtenerConexion()) {

            // --- 1. CONSULTAMOS LOS DATOS DE LA CUENTA ---
            try (PreparedStatement psC = con.prepareStatement(sqlCuenta)) {
                psC.setInt(1, cuentaId);
                ResultSet rsC = psC.executeQuery(); // Aquí se crea rsC

                if (rsC.next()) { // Bloque donde rsC ES VÁLIDO

                    // Extraemos los datos a variables locales (Más seguro)
                    String titular = rsC.getString("titular");
                    String cedula = rsC.getString("cedula");
                    String direccion = rsC.getString("direccion");
                    String telefono = rsC.getString("telefono");

                    // --- 2. CONFIGURAMOS EL PDF (DENTRO del bloque donde rsC existe) ---
                    PdfWriter writer = new PdfWriter(nombreArchivo);
                    PdfDocument pdf = new PdfDocument(writer);
                    Document documento = new Document(pdf);

                    // Escribimos en el PDF usando las variables que acabamos de sacar
                    documento.add(new Paragraph("JAVABANK SYSTEM - REPORTE OFICIAL").setBold().setFontSize(18));
                    documento.add(new Paragraph("Titular: " + titular));
                    documento.add(new Paragraph("Cédula/RIF: " + cedula));
                    documento.add(new Paragraph("Dirección: " + direccion));
                    documento.add(new Paragraph("Teléfono: " + telefono));
                    documento.add(new Paragraph("\n"));

                    // --- 3. TABLA DE MOVIMIENTOS ---
                    Table tabla = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2})).useAllAvailableWidth();
                    tabla.addHeaderCell("Fecha");
                    tabla.addHeaderCell("Monto");
                    tabla.addHeaderCell("Operación");

                    try (PreparedStatement psT = con.prepareStatement(sqlTrans)) {
                        psT.setInt(1, cuentaId);
                        ResultSet rsT = psT.executeQuery();
                        while (rsT.next()) {
                            tabla.addCell(rsT.getTimestamp("fecha_hora").toString());
                            tabla.addCell(String.format("%.2f", rsT.getDouble("monto")));
                            tabla.addCell(rsT.getString("tipo"));
                        }
                    }

                    documento.add(tabla);
                    documento.close();
                    System.out.println("PDF generado con éxito.");

                } else {
                    System.out.println("No se encontró información para la cuenta ID: " + cuentaId);
                }
            } // Aquí rsC se cierra automáticamente al salir del try-with-resources

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace(); // Dirá exactamente qué línea falla
        }
    }

    // 2. CREAR CUENTA
    public void crearCuenta(Cuenta cuenta) {
        String sql = "INSERT INTO cuentas (id, numero_cuenta, titular, cedula, direccion, telefono, saldo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVO')";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cuenta.getId());
            ps.setString(2, cuenta.getNumeroCuenta());
            ps.setString(3, cuenta.getTitular());
            ps.setString(4, cuenta.getCedula());
            ps.setString(5, cuenta.getDireccion());
            ps.setString(6, cuenta.getTelefono());
            ps.setDouble(7, cuenta.getSaldo());
            ps.executeUpdate();
            System.out.println("Cuenta creada con éxito.");
        } catch (SQLException e) {
            System.out.println("Error al crear cuenta: " + e.getMessage());
        }
    }

    // 3. CERRAR CUENTA
    public void cerrarCuenta(int id) {
        // Cambio: En lugar de borrar, cambiamos el estado
        String sql = "UPDATE cuentas SET estado = 'CERRADO' WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("La cuenta ID " + id + " ha sido CERRADA (sus datos permanecen para auditoría).");
            } else {
                System.out.println("No se encontró la cuenta.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4. ACTUALIZAR SALDO (Muy importante para depósitos/retiros)
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

    //5. CONSULTAR SALDO

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

    //DEPOSITO (TRANSACCIONES)

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
            // Desactivamos el auto-commit para manejar la transacción manualmente
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
                    con.rollback(); // Si la cuenta no existe, deshacemos todo
                    System.out.println("Error: No se encontró la cuenta con ID " + cuentaId);
                }

            } catch (SQLException e) {
                con.rollback(); // Si hay error en el SQL, deshacemos todo
                System.out.println("Error en la transacción: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    //RETIRO (TRANSACCIONES)

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
            con.setAutoCommit(false); // Iniciamos transacción manual

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

                        con.commit(); // TODO BIEN, SE APLICAN CAMBIOS
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

    //-----------------------------TRANSFERENCIAS-------------------------
    //--------------------------------------------------------------------

    public void transferir(int idOrigen, int idDestino, double monto) {
        if (monto <= 0) {
            System.out.println("El monto debe ser mayor a cero.");
            return;
        }
        if (idOrigen == idDestino) {
            System.out.println("No puedes transferir a la misma cuenta.");
            return;
        }

        if (!esCuentaActiva(idOrigen)) return;
        if (!esCuentaActiva(idDestino)) return;

        String sqlCheck = "SELECT saldo FROM cuentas WHERE id = ?";
        String sqlRestar = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";
        String sqlSumar = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";
        String sqlHistorial = "INSERT INTO transacciones (cuenta_id, tipo, monto) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false); // SE INICIA LA ZONA DE SEGURIDAD

            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck);
                 PreparedStatement psRestar = con.prepareStatement(sqlRestar);
                 PreparedStatement psSumar = con.prepareStatement(sqlSumar);
                 PreparedStatement psHistorial = con.prepareStatement(sqlHistorial)) {

                // 1. Verificar saldo de origen
                psCheck.setInt(1, idOrigen);
                ResultSet rs = psCheck.executeQuery();

                if (!rs.next() || rs.getDouble("saldo") < monto) {
                    System.out.println("Transferencia fallida: Saldo insuficiente o cuenta origen no existe.");
                    con.rollback();
                    return;
                }

                // 2. Restar de Origen
                psRestar.setDouble(1, monto);
                psRestar.setInt(2, idOrigen);
                int filasO = psRestar.executeUpdate();

                // 3. Sumar a Destino
                psSumar.setDouble(1, monto);
                psSumar.setInt(2, idDestino);
                int filasD = psSumar.executeUpdate();

                if (filasO > 0 && filasD > 0) {
                    // 4. Registrar ambos movimientos en el historial
                    // Registro para el que envía
                    psHistorial.setInt(1, idOrigen);
                    psHistorial.setString(2, "TRANSFERENCIA_SALIDA");
                    psHistorial.setDouble(3, monto);
                    psHistorial.executeUpdate();

                    // Registro para el que recibe
                    psHistorial.setInt(1, idDestino);
                    psHistorial.setString(2, "TRANSFERENCIA_ENTRADA");
                    psHistorial.setDouble(3, monto);
                    psHistorial.executeUpdate();

                    con.commit(); // Se aplican todos los cambios
                    System.out.println("Transferencia de $" + monto + " realizada con éxito.");
                } else {
                    System.out.println("Error: Una de las cuentas no existe.");
                    con.rollback();
                }

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error crítico en la transferencia: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    //------------------REPORTE BANCARIO-----------------

    public void verExtracto(int cuentaId) {
        // Primero verificamos si la cuenta existe para dar un mensaje claro
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

    // Este método centraliza la validación de estado
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
        return false; // Si no existe o no está activa, bloqueamos la operación
    }

    //-----------------------------ANEXANDO CODIGO PARA MEJORAR CONTROL DE CUENTAS--------


    //AUDITORIAS-------------------
    // Para ver la lista completa

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

    // Para reactivar una cuenta cerrada
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

    //--------------------USUARIO---------------------

    public Usuario login(String user, String pass) {
        String sql = "SELECT username, rol FROM usuarios WHERE username = ? AND password = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Retornamos un nuevo objeto Usuario con los datos de la DB
                return new Usuario(rs.getString("username"), rs.getString("rol"));
            }
        } catch (SQLException e) {
            System.out.println("Error en base de datos durante login: " + e.getMessage());
        }
        return null; // Si no lo encuentra o hay error, devuelve null
    }

    //===========================INTERESES===========================
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
}