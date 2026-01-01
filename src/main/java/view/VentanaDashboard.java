package view;
import com.bank.util.ConexionBD;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.Font;
import com.itextpdf.text.*;
import java.io.FileOutputStream;


public class VentanaDashboard extends JFrame {
    private int usuarioId;

    public VentanaDashboard(int usuarioId) {
        this.usuarioId = usuarioId;

        setTitle("JavaBank - Panel de Control");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Panel Lateral ---
        JPanel panelMenu = new JPanel(new GridLayout(0, 1, 5, 5));
        panelMenu.setBackground(new Color(44, 62, 80));
        panelMenu.setPreferredSize(new Dimension(230, 500));

        // Botones
        JButton btnNuevaCuenta = crearBotonMenu("Abrir Cuenta Nueva");
        JButton btnVerCuentas = crearBotonMenu("Ver Cuentas Bancarias");
        JButton btnSaldo = crearBotonMenu("Consultar Saldo");
        JButton btnDeposito = crearBotonMenu("Depósito Dinero");
        JButton btnRetirar = crearBotonMenu("Retirar Efectivo");
        JButton btnTransferir = crearBotonMenu("Transferir");
        JButton btnCerrarCuenta = crearBotonMenu("Cerrar Cuenta (Soft Delete)");
        JButton btnExtracto = crearBotonMenu("Ver Extracto Bancario");
        JButton btnAdmin = crearBotonMenu("Panel de Administración");
        JButton btnPDF = crearBotonMenu("Generar Reporte PDF");
        JButton btnCierre = crearBotonMenu("Ejecutar Cierre de Mes");
        JButton btnLimites = crearBotonMenu("Disponibilidad y Límites");
        JButton btnPrestamos = crearBotonMenu("Evaluar Préstamos");
        JButton btnSalir = crearBotonMenu("Cerrar Sesión");
        btnSalir.setBackground(new Color(192, 57, 43));

        btnNuevaCuenta.addActionListener(e -> ejecutarNuevaCuenta());
        btnVerCuentas.addActionListener(e -> mostrarListadoCuentas());
        btnCerrarCuenta.addActionListener(e -> ejecutarCerrarCuenta());
        btnSaldo.addActionListener(e -> mostrarSaldo());
        btnDeposito.addActionListener(e -> ejecutarDeposito());
        btnRetirar.addActionListener(e -> ejecutarRetiro());
        btnTransferir.addActionListener(e -> ejecutarTransferencia());
        btnExtracto.addActionListener(e -> mostrarExtractoBancario());
        btnAdmin.addActionListener(e -> mostrarPanelAdmin());
        btnPDF.addActionListener(e -> generarReportePDF());
        btnCierre.addActionListener(e -> ejecutarCierreMes());
        btnLimites.addActionListener(e -> mostrarDisponibilidadYLimites());
        btnPrestamos.addActionListener(e -> evaluarPrestamos());



        btnSalir.addActionListener(e -> {
            new VentanaLogin().setVisible(true);
            this.dispose();
        });

        panelMenu.add(new JLabel("<html><font color='white'><b> OPERACIONES</b>", SwingConstants.CENTER));
        panelMenu.add(btnNuevaCuenta); //Boton 1
        panelMenu.add(btnVerCuentas);//Boton 2 ver cuentas
        panelMenu.add(btnCerrarCuenta); // Botón 3
        panelMenu.add(btnSaldo); // Botón 4
        panelMenu.add(btnDeposito); // Botón 5
        panelMenu.add(btnRetirar); // Botón 6
        panelMenu.add(btnTransferir); // Botón 7
        panelMenu.add(btnExtracto); //Boton 8 Ver extracto bancario
        panelMenu.add(btnAdmin); //Boton 9 Auditoria/reactivar
        panelMenu.add(btnPDF); //Boton 10 Generar PDF
        panelMenu.add(btnCierre); //Boton 11 Ejecutar cierre de mes
        panelMenu.add(btnLimites); //Boton 12 Disponibilidad y limite de la cuenta
        panelMenu.add(btnPrestamos);//Boton 13 Evaluar prestamos
        panelMenu.add(btnSalir); //Boton 14

        add(panelMenu, BorderLayout.WEST);

        // Panel central de bienvenida
        JPanel panelInicio = new JPanel(new GridBagLayout());
        panelInicio.add(new JLabel("Bienvenido al sistema. Seleccione una operación."));
        add(panelInicio, BorderLayout.CENTER);
    }


    //==========================CREAR CUENTA NUEVA================================
    //=========================CASE 1---------------------------

    //==========================CREAR CUENTA NUEVA ============================
    private void ejecutarNuevaCuenta() {
        String[] opciones = {"Ahorros", "Corriente", "Nomina"};
        String tipo = (String) JOptionPane.showInputDialog(this, "Tipo de cuenta:", "Nueva Cuenta",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (tipo == null) return;

        String montoStr = JOptionPane.showInputDialog(this, "Monto de apertura ($10.0 min):");
        if (montoStr == null || montoStr.isEmpty()) return;

        try {
            double montoInicial = Double.parseDouble(montoStr);
            String numCuenta = "CTA-" + (int)(Math.random() * 900000 + 100000);

            try (Connection con = ConexionBD.obtenerConexion()) {
                String nombre = "";
                String sqlUser = "SELECT username FROM usuarios WHERE id = ?";

                try (PreparedStatement psUser = con.prepareStatement(sqlUser)) {
                    psUser.setInt(1, usuarioId);
                    ResultSet rs = psUser.executeQuery();
                    if (rs.next()) {
                        nombre = rs.getString("username");
                    }
                }

                String cedula = JOptionPane.showInputDialog(this, "Ingrese Cédula del titular:");
                if (cedula == null || cedula.isEmpty()) return;

                String dir = JOptionPane.showInputDialog(this, "Ingrese Dirección:");
                if (dir == null) dir = "No especificada";

                String tel = JOptionPane.showInputDialog(this, "Ingrese Teléfono:");
                if (tel == null) tel = "0000";

                String sql = "INSERT INTO cuentas (numero_cuenta, titular, cedula, direccion, telefono, saldo, estado, usuario_id, tipo_cuenta) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVO', ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, numCuenta);
                    ps.setString(2, nombre);
                    ps.setString(3, cedula);
                    ps.setString(4, dir);
                    ps.setString(5, tel);
                    ps.setDouble(6, montoInicial);
                    ps.setInt(7, usuarioId);
                    ps.setString(8, tipo); // <--- AQUÍ PASAMOS EL VALOR ELEGIDO (Nomina, Corriente, etc.)

                    ps.executeUpdate();

                    registrarAuditoria(con, "APERTURA_CUENTA", "Cuenta " + tipo + " " + numCuenta + " creada.");
                    JOptionPane.showMessageDialog(this, "¡Éxito! Cuenta de tipo " + tipo + " abierta.");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Crítico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //==========================MOSTRAR LISTADO DE CUENTAS======================
    //=====================CASE 2-----------------------------------

    private void mostrarListadoCuentas() {
        String[] columnas = {"Cuenta #", "Tipo", "Titular", "Cédula", "Saldo", "Estado"};

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (Connection con = ConexionBD.obtenerConexion()) {
            String sql = "SELECT numero_cuenta, tipo_cuenta, titular, cedula, saldo, estado FROM cuentas WHERE usuario_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] fila = {
                        rs.getString("numero_cuenta"),
                        rs.getString("tipo_cuenta"),
                        rs.getString("titular"),
                        rs.getString("cedula"),
                        String.format("$%.2f", rs.getDouble("saldo")), // Formato moneda
                        rs.getString("estado")
                };
                modelo.addRow(fila);
            }

            JTable tabla = new JTable(modelo);
            tabla.setFillsViewportHeight(true);
            tabla.setRowHeight(25); // Filas un poco más altas para mejor lectura

            tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

            JScrollPane scrollPane = new JScrollPane(tabla);
            scrollPane.setPreferredSize(new Dimension(700, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Mis Cuentas Registradas", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la lista: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //==========================CASE 3 ==================================

    private void ejecutarCerrarCuenta() {
        String numCuenta = JOptionPane.showInputDialog(this, "Número de cuenta a CERRAR:");
        if (numCuenta == null || numCuenta.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cerrar la cuenta " + numCuenta + "?\nEsta acción es reversible por un administrador.",
                "Confirmar Cierre", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Usamos 'INACTIVO' que es un estándar bancario
            String sql = "UPDATE cuentas SET estado = 'INACTIVO' WHERE numero_cuenta = ? AND usuario_id = ?";

            try (Connection con = ConexionBD.obtenerConexion()) {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, numCuenta);
                ps.setInt(2, usuarioId);

                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    registrarAuditoria(con, "SOFT_DELETE", "Cuenta cerrada: " + numCuenta);
                    JOptionPane.showMessageDialog(this, "Cuenta " + numCuenta + " cerrada exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró la cuenta o no tiene permisos.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

//===============================CONSULTAR SALDO==========================
//==============================FUNCION 4================================

    private void mostrarSaldo() {
        String sql = "SELECT numero_cuenta, saldo, tipo_cuenta FROM cuentas WHERE usuario_id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            boolean tieneCuentas = false;

            while (rs.next()) {
                tieneCuentas = true;
                sb.append("CUENTA: ").append(rs.getString("numero_cuenta")).append("\n");
                sb.append("Tipo: ").append(rs.getString("tipo_cuenta")).append("\n");
                sb.append("Saldo: $").append(String.format("%.2f", rs.getDouble("saldo"))).append("\n");
                sb.append("--------------------------------\n");
            }

            if (tieneCuentas) {
                // 1. Creamos un JTextArea para contener el texto
                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false); // Que el usuario no pueda borrar el saldo
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Fuente tipo recibo
                textArea.setBackground(new Color(245, 245, 245));

                // 2. Lo metemos dentro de un JScrollPane
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(350, 250)); // Tamaño fijo de la ventanita

                // 3. Mostramos el ScrollPane en lugar del String directo
                JOptionPane.showMessageDialog(this, scrollPane, "Mis Cuentas Registradas", JOptionPane.PLAIN_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "No tienes cuentas activas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        return btn;
    }

    //=========================CASE 5 DEPOSITAR================================

    private void ejecutarDeposito() {
        String numCuenta = JOptionPane.showInputDialog(this, "Número de cuenta para depósito:");
        if (numCuenta == null || numCuenta.isEmpty()) return;

        String montoStr = JOptionPane.showInputDialog(this, "Monto a depositar:");
        if (montoStr == null || montoStr.isEmpty()) return;

        try {
            double monto = Double.parseDouble(montoStr);
            if (monto <= 0) throw new NumberFormatException();

            try (Connection con = ConexionBD.obtenerConexion()) {
                String sql = "UPDATE cuentas SET saldo = saldo + ? WHERE numero_cuenta = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setDouble(1, monto);
                ps.setString(2, numCuenta);

                if (ps.executeUpdate() > 0) {
                    registrarAuditoria(con, "DEPÓSITO", "Depósito de $" + monto + " a cuenta " + numCuenta);
                    JOptionPane.showMessageDialog(this, "Depósito exitoso.");
                } else {
                    JOptionPane.showMessageDialog(this, "Cuenta no encontrada.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto inválido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    //=======================FUNCIONALIDAD 7: PARA RETIRO=================


    private void ejecutarRetiro() {
        String numCuenta = JOptionPane.showInputDialog(this, "Ingrese el número de cuenta:");
        if (numCuenta == null || numCuenta.isEmpty()) return;

        String montoStr = JOptionPane.showInputDialog(this, "Monto a retirar:");
        if (montoStr == null || montoStr.isEmpty()) return;

        try {
            double monto = Double.parseDouble(montoStr);

            try (Connection con = ConexionBD.obtenerConexion()) {
                con.setAutoCommit(false);

                // 1. Obtenemos saldo y tipo para las validaciones
                String sqlCheck = "SELECT saldo, tipo_cuenta FROM cuentas WHERE numero_cuenta = ? AND usuario_id = ?";
                PreparedStatement psCheck = con.prepareStatement(sqlCheck);
                psCheck.setString(1, numCuenta);
                psCheck.setInt(2, usuarioId);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    double saldoActual = rs.getDouble("saldo");
                    String tipo = rs.getString("tipo_cuenta").toUpperCase().trim();

                    // --- BLOQUE DE VALIDACIONES ---

                    // Regla 1: Bloqueo de monto máximo para Ahorros
                    if (tipo.contains("AHORRO") && monto > 500.0) {
                        JOptionPane.showMessageDialog(this,
                                "OPERACIÓN RECHAZADA: Las cuentas de Ahorros tienen un límite de $500 por retiro.",
                                "Seguridad", JOptionPane.WARNING_MESSAGE);
                        con.rollback();
                        return;
                    }

                    // Regla 2: Cálculo de límite disponible (incluyendo sobregiro)
                    double limiteMaximo = tipo.contains("CORRIENTE") ? (saldoActual + 200.0) : saldoActual;

                    // Regla 3: Verificación de saldo y reserva de $10
                    if (limiteMaximo - monto < 10.0) {
                        String motivo = tipo.contains("CORRIENTE") ?
                                "Excede el sobregiro de $200 y la reserva de $10." :
                                "El saldo debe quedar al menos en $10.0";

                        JOptionPane.showMessageDialog(this, "Saldo insuficiente: " + motivo,
                                "Error de Fondos", JOptionPane.ERROR_MESSAGE);
                        con.rollback();
                        return;
                    }

                    // --- FIN DE VALIDACIONES ---

                    // 2. Si llegamos aquí, la operación es legal. Ejecutamos:
                    String sqlUpdate = "UPDATE cuentas SET saldo = saldo - ? WHERE numero_cuenta = ?";
                    PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                    psUpdate.setDouble(1, monto);
                    psUpdate.setString(2, numCuenta);
                    psUpdate.executeUpdate();

                    registrarAuditoria(con, "RETIRO EXITOSO", "Cuenta: " + numCuenta + " - Monto: $" + monto);

                    con.commit();
                    JOptionPane.showMessageDialog(this, "Retiro procesado.\nNuevo saldo: $" + (saldoActual - monto));
                } else {
                    JOptionPane.showMessageDialog(this, "La cuenta no existe o no tiene permisos.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage());
        }
    }

    // Método auxiliar para mantener tus logs al día
    private void registrarAuditoria(Connection con, String accion, String detalles) throws SQLException {
        String sql = "INSERT INTO logs_sistema (usuario_id, accion, detalles, fecha_hora) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, detalles);
            ps.executeUpdate();
        }
    }

    //====================FUNCION7:  TRANSFERENCIA Y SU LOGICA======================
    //============================================================================

    private void ejecutarTransferencia() {
        String cuentaOrigen = JOptionPane.showInputDialog(this, "Número de cuenta de ORIGEN:");
        if (cuentaOrigen == null || cuentaOrigen.isEmpty()) return;

        String cuentaDestino = JOptionPane.showInputDialog(this, "Número de cuenta de DESTINO:");
        if (cuentaDestino == null || cuentaDestino.isEmpty()) return;

        String montoStr = JOptionPane.showInputDialog(this, "Monto a transferir:");
        if (montoStr == null || montoStr.isEmpty()) return;

        try {
            double monto = Double.parseDouble(montoStr);

            try (Connection con = ConexionBD.obtenerConexion()) {
                con.setAutoCommit(false); // IMPORTANTE: Iniciamos transacción

                // 1. Verificar saldo en origen y que pertenezca al usuario
                String sqlOrigen = "SELECT saldo FROM cuentas WHERE numero_cuenta = ? AND usuario_id = ?";
                PreparedStatement psOrigen = con.prepareStatement(sqlOrigen);
                psOrigen.setString(1, cuentaOrigen);
                psOrigen.setInt(2, usuarioId);
                ResultSet rsO = psOrigen.executeQuery();

                if (rsO.next()) {
                    double saldoActual = rsO.getDouble("saldo");

                    // Regla de saldo mínimo ($10.0)
                    if (saldoActual - monto < 10.0) {
                        JOptionPane.showMessageDialog(this, "Saldo insuficiente. Debe quedar al menos $10.0");
                        con.rollback();
                        return;
                    }

                    // 2. Verificar que la cuenta destino exista
                    String sqlDestino = "SELECT id FROM cuentas WHERE numero_cuenta = ?";
                    PreparedStatement psDestino = con.prepareStatement(sqlDestino);
                    psDestino.setString(1, cuentaDestino);
                    ResultSet rsD = psDestino.executeQuery();

                    if (rsD.next()) {
                        // 3. DESCONTAR de origen
                        String sqlResta = "UPDATE cuentas SET saldo = saldo - ? WHERE numero_cuenta = ?";
                        PreparedStatement psResta = con.prepareStatement(sqlResta);
                        psResta.setDouble(1, monto);
                        psResta.setString(2, cuentaOrigen);
                        psResta.executeUpdate();

                        // 4. SUMAR a destino
                        String sqlSuma = "UPDATE cuentas SET saldo = saldo + ? WHERE numero_cuenta = ?";
                        PreparedStatement psSuma = con.prepareStatement(sqlSuma);
                        psSuma.setDouble(1, monto);
                        psSuma.setString(2, cuentaDestino);
                        psSuma.executeUpdate();

                        // 5. AUDITORÍA (usando tu columna 'detalles')
                        registrarAuditoria(con, "TRANSFERENCIA ENVIADA",
                                "De: " + cuentaOrigen + " A: " + cuentaDestino + " Monto: $" + monto);

                        con.commit(); // ¡Todo salió bien, guardamos!
                        JOptionPane.showMessageDialog(this, "Transferencia realizada con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(this, "La cuenta de destino no existe.");
                        con.rollback();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "La cuenta de origen no es válida.");
                    con.rollback();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto inválido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    //=========================EXTRACTO BANCARIO========================
    //===============CASE 8---------------------------------------------

    private void mostrarExtractoBancario() {
        // 1. Pedir el número de cuenta para filtrar
        String numCuenta = JOptionPane.showInputDialog(this, "Ingrese el número de cuenta para ver el extracto:");
        if (numCuenta == null || numCuenta.isEmpty()) return;

        // 2. Configurar la tabla de movimientos
        String[] columnas = {"Fecha y Hora", "Operación", "Detalles del Movimiento"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        // Usamos un LIKE en SQL para buscar el número de cuenta dentro del texto de 'detalles'
        String sql = "SELECT fecha_hora, accion, detalles FROM logs_sistema " +
                "WHERE usuario_id = ? AND detalles LIKE ? " +
                "ORDER BY fecha_hora DESC";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, "%" + numCuenta + "%"); // Busca el número de cuenta en cualquier parte del texto

            ResultSet rs = ps.executeQuery();
            boolean tieneMovimientos = false;

            while (rs.next()) {
                tieneMovimientos = true;
                Object[] fila = {
                        rs.getTimestamp("fecha_hora"),
                        rs.getString("accion"),
                        rs.getString("detalles")
                };
                modelo.addRow(fila);
            }

            if (!tieneMovimientos) {
                JOptionPane.showMessageDialog(this, "No se encontraron movimientos para la cuenta: " + numCuenta);
                return;
            }

            // 3. Diseño de la tabla
            JTable tabla = new JTable(modelo);
            tabla.getColumnModel().getColumn(0).setPreferredWidth(150);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(120);
            tabla.getColumnModel().getColumn(2).setPreferredWidth(300);

            JScrollPane scroll = new JScrollPane(tabla);
            scroll.setPreferredSize(new Dimension(650, 300));

            JOptionPane.showMessageDialog(this, scroll, "Extracto Bancario - Cuenta " + numCuenta, JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar extracto: " + e.getMessage());
        }
    }

    //==========================FUNCION 9: AUDITORIAS/REACTIVACION=============

    private void mostrarPanelAdmin() {
        StringBuilder auditoriaStr = new StringBuilder("--- AUDITORÍA DE SISTEMA (SYSTEM_LOGS) ---\n\n");

        try (Connection con = ConexionBD.obtenerConexion()) {
            String sqlLogs = "SELECT s.accion, s.detalles, s.fecha_hora, u.username " +
                    "FROM logs_sistema s " +
                    "INNER JOIN usuarios u ON s.usuario_id = u.id " +
                    "ORDER BY s.fecha_hora DESC LIMIT 15";

            try (PreparedStatement psL = con.prepareStatement(sqlLogs);
                 ResultSet rsL = psL.executeQuery()) {

                while (rsL.next()) {
                    auditoriaStr.append("[").append(rsL.getTimestamp("fecha_hora")).append("] ")
                            .append(rsL.getString("username")).append(" -> ")
                            .append(rsL.getString("accion")).append(": ")
                            .append(rsL.getString("detalles")).append("\n");
                }
            }

            // Interfaz
            JTextArea areaTexto = new JTextArea(auditoriaStr.toString(), 15, 60);
            areaTexto.setEditable(false);
            areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));

            Object[] opciones = {"Reactivar Cuenta", "Cerrar"};
            int seleccion = JOptionPane.showOptionDialog(this,
                    new JScrollPane(areaTexto),
                    "Módulo de Auditoría y Seguridad",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[1]);

            if (seleccion == JOptionPane.YES_OPTION) {
                String idStr = JOptionPane.showInputDialog(this, "ID de la cuenta a REACTIVAR:");
                if (idStr != null && !idStr.isEmpty()) {
                    // Tu método guía usa el ID numérico
                    String sqlReactivar = "UPDATE cuentas SET estado = 'ACTIVO' WHERE id = ?";
                    try (PreparedStatement psR = con.prepareStatement(sqlReactivar)) {
                        psR.setInt(1, Integer.parseInt(idStr));
                        int filas = psR.executeUpdate();
                        if (filas > 0) {
                            JOptionPane.showMessageDialog(this, "Cuenta reactivada exitosamente.");
                        } else {
                            JOptionPane.showMessageDialog(this, "No se encontró el ID.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de acceso: " + e.getMessage());
        }
    }

    //==========================FUNCION 10: GENERAR PDF========================

    private void generarReportePDF() {
        String ruta = System.getProperty("user.home") + "/Desktop/Estado_Cuenta_Premium.pdf";
        com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new FileOutputStream(ruta));
            documento.open();

            // --- DEFINICIÓN DE FUENTES Y COLORES ---
            com.itextpdf.text.BaseColor azulOscuro = new com.itextpdf.text.BaseColor(0, 51, 102);
            com.itextpdf.text.BaseColor grisClaro = new com.itextpdf.text.BaseColor(240, 240, 240);
            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 22, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fuenteHeader = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.WHITE);

            // --- 1. BANNER DE ENCABEZADO ---
            com.itextpdf.text.pdf.PdfPTable banner = new com.itextpdf.text.pdf.PdfPTable(1);
            banner.setWidthPercentage(100);
            com.itextpdf.text.pdf.PdfPCell celdaTitulo = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("JAVABANK SYSTEM", fuenteTitulo));
            celdaTitulo.setBackgroundColor(azulOscuro);
            celdaTitulo.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
            celdaTitulo.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            celdaTitulo.setPadding(15);
            banner.addCell(celdaTitulo);
            documento.add(banner);

            documento.add(new com.itextpdf.text.Paragraph("\n"));

            // --- 2. INFORMACIÓN DEL TITULAR ---
            try (Connection con = ConexionBD.obtenerConexion()) {
                String sqlUser = "SELECT username FROM usuarios WHERE id = ?";
                PreparedStatement psU = con.prepareStatement(sqlUser);
                psU.setInt(1, usuarioId);
                ResultSet rsU = psU.executeQuery();

                if (rsU.next()) {
                    String nombreParaReporte = rsU.getString("username").toUpperCase();

                    documento.add(new com.itextpdf.text.Paragraph("RESUMEN DE CUENTAS PARA: " + nombreParaReporte,
                            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14)));
                    documento.add(new com.itextpdf.text.Paragraph("Fecha de emisión: " + new java.util.Date()));
                    documento.add(new com.itextpdf.text.Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
                }

                documento.add(new com.itextpdf.text.Paragraph("\n"));

                // --- 3. TABLA DE PRODUCTOS (CUENTAS) ---
                com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(4);
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(10f);

                String[] headers = {"Número de Cuenta", "Tipo de Producto", "Estado", "Saldo Disponible"};
                for (String h : headers) {
                    com.itextpdf.text.pdf.PdfPCell hCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h, fuenteHeader));
                    hCell.setBackgroundColor(azulOscuro);
                    hCell.setPadding(8);
                    hCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    tabla.addCell(hCell);
                }

                String sqlCuentas = "SELECT numero_cuenta, tipo_cuenta, estado, saldo FROM cuentas WHERE usuario_id = ?";
                PreparedStatement psC = con.prepareStatement(sqlCuentas);
                psC.setInt(1, usuarioId);
                ResultSet rsC = psC.executeQuery();

                boolean alterna = false;
                while (rsC.next()) {
                    com.itextpdf.text.BaseColor fondoFila = alterna ? grisClaro : com.itextpdf.text.BaseColor.WHITE;

                    tabla.addCell(crearCeldaDato(rsC.getString("numero_cuenta"), fondoFila));
                    tabla.addCell(crearCeldaDato(rsC.getString("tipo_cuenta"), fondoFila));
                    tabla.addCell(crearCeldaDato(rsC.getString("estado"), fondoFila));
                    tabla.addCell(crearCeldaDato("$ " + String.format("%.2f", rsC.getDouble("saldo")), fondoFila));

                    alterna = !alterna;
                }
                documento.add(tabla);
            }

            // --- 4. PIE DE PÁGINA ---
            documento.add(new com.itextpdf.text.Paragraph("\n\n\n"));
            com.itextpdf.text.Paragraph aviso = new com.itextpdf.text.Paragraph(
                    "Este documento es un reporte generado automáticamente y sirve como comprobante de saldos a la fecha.\nJavaBank System 2025 - Todos los derechos reservados.",
                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 8, com.itextpdf.text.BaseColor.GRAY)
            );
            aviso.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(aviso);

            documento.close();
            JOptionPane.showMessageDialog(this, "Reporte Premium generado exitosamente.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar para no repetir código de diseño de celdas
    private com.itextpdf.text.pdf.PdfPCell crearCeldaDato(String texto, com.itextpdf.text.BaseColor colorFondo) {
        com.itextpdf.text.pdf.PdfPCell celda = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(texto,
                com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10)));
        celda.setBackgroundColor(colorFondo);
        celda.setPadding(6);
        celda.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        celda.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        return celda;
    }

    //=========================FUNCION 11 APLICACION DE INTERES=================
    //----------------------CASE 11-----------------

    private void ejecutarCierreMes() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea ejecutar el cierre de mes?\nSe aplicará un 0.5% de interés a las cuentas de Ahorros.",
                "Proceso de Cierre", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sqlUpdate = "UPDATE cuentas SET saldo = saldo + (saldo * 0.005) " +
                "WHERE UPPER(tipo_cuenta) LIKE 'AHORRO%' " +
                "AND saldo > 0 " +
                "AND UPPER(estado) = 'ACTIVO' " +
                "AND usuario_id = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setInt(1, usuarioId);

                int cuentasAfectadas = ps.executeUpdate();

                if (cuentasAfectadas > 0) {
                    registrarAuditoria(con, "CIERRE_MES", "Intereses aplicados a " + cuentasAfectadas + " cuentas.");
                    con.commit();
                    JOptionPane.showMessageDialog(this, "¡Cierre completado!\nCuentas actualizadas: " + cuentasAfectadas);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se detectaron cuentas aptas.\nVerifique que existan cuentas de 'AHORROS' con estado 'ACTIVO' y saldo > 0.");
                    con.rollback();
                }
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error en el cierre: " + e.getMessage());
        }
    }

    //==========================FUNCION DE 12: CIERRE DE MES=======================
    //=============================================================================
    private void mostrarDisponibilidadYLimites() {
        String numCuenta = JOptionPane.showInputDialog(this, "Ingrese el número de cuenta a consultar:");
        if (numCuenta == null || numCuenta.isEmpty()) return;

        try (Connection con = ConexionBD.obtenerConexion()) {
            String sql = "SELECT tipo_cuenta, saldo FROM cuentas WHERE numero_cuenta = ? AND usuario_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, numCuenta);
            ps.setInt(2, usuarioId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String tipo = rs.getString("tipo_cuenta");
                double saldo = rs.getDouble("saldo");
                String mensaje = "Análisis de Disponibilidad para: " + numCuenta + "\n\n";
                mensaje += "Saldo Actual: $" + saldo + "\n";
                mensaje += "Tipo de Cuenta: " + tipo + "\n";
                mensaje += "--------------------------------------\n";

                if (tipo.equalsIgnoreCase("Ahorros")) {
                    mensaje += "• Límite de retiro por operación: $500.00\n";
                    mensaje += "• Disponible para retiro hoy: $" + Math.min(saldo, 500.00) + "\n";
                    mensaje += "• Intereses devengados: 0.5% mensual.";
                } else if (tipo.equalsIgnoreCase("Corriente")) {
                    mensaje += "• Límite de Sobregiro: $200.00\n";
                    mensaje += "• Capacidad total de pago (Saldo + Sobregiro): $" + (saldo + 200.00) + "\n";
                    mensaje += "• Comisión por uso de sobregiro: 2%.";
                } else {
                    mensaje += "• Cuenta de Nómina: Sin límites especiales.";
                }

                JOptionPane.showMessageDialog(this, mensaje, "Límites y Disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cuenta no encontrada.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    //============================EVALUAR PRESTAMOS==========================
    //----------------------------------------------------------------------
    //--------FUNCION 13

    private void evaluarPrestamos() {
        double saldoTotal = 0;
        int cantidadCuentas = 0;

        try (Connection con = ConexionBD.obtenerConexion()) {
            String sql = "SELECT SUM(saldo) as total, COUNT(*) as cantidad FROM cuentas WHERE usuario_id = ? AND estado = 'ACTIVO'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                saldoTotal = rs.getDouble("total");
                cantidadCuentas = rs.getInt("cantidad");
            }

            // 2. Lógica del Algoritmo de Crédito
            StringBuilder mensaje = new StringBuilder("--- ANÁLISIS DE CRÉDITO ---\n\n");
            mensaje.append("Saldo Total Consolidado: $").append(String.format("%.2f", saldoTotal)).append("\n");
            mensaje.append("Cuentas Activas: ").append(cantidadCuentas).append("\n\n");

            if (cantidadCuentas == 0) {
                mensaje.append("Resultado: RECHAZADO\nMotivo: No tiene cuentas activas.");
            } else if (saldoTotal < 1000) {
                mensaje.append("Resultado: EN OBSERVACIÓN\nMotivo: El saldo total debe ser mayor a $1,000 para calificar.");
            } else if (saldoTotal >= 1000 && saldoTotal < 5000) {
                double preAprobado = saldoTotal * 2; // El banco presta el doble de lo que tienes
                mensaje.append("Resultado: ¡APROBADO!\n");
                mensaje.append("Monto Pre-aprobado: $").append(String.format("%.2f", preAprobado)).append("\n");
                mensaje.append("Tasa de interés: 12% E.A.");
            } else {
                // Clientes VIP (Más de $5,000)
                double preAprobado = saldoTotal * 5;
                mensaje.append("Resultado: ¡CLIENTE VIP - APROBADO!\n");
                mensaje.append("Monto Pre-aprobado: $").append(String.format("%.2f", preAprobado)).append("\n");
                mensaje.append("Tasa Preferencial: 8% E.A.");
            }

            JOptionPane.showMessageDialog(this, mensaje.toString(), "Evaluación Crediticia", JOptionPane.INFORMATION_MESSAGE);

            // Opcional: Registrar la consulta en la auditoría
            registrarAuditoria(con, "EVALUACION_PRESTAMO", "Saldo analizado: $" + saldoTotal);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al evaluar crédito: " + e.getMessage());
        }
    }

}