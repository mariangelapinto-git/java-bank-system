package view;
import com.bank.util.ConexionBD;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        JPanel panelMenu = new JPanel(new GridLayout(6, 1, 10, 10));
        panelMenu.setBackground(new Color(44, 62, 80));
        panelMenu.setPreferredSize(new Dimension(200, 500));

        // Botones
        JButton btnSaldo = crearBotonMenu("Consultar Saldo");
        JButton btnRetirar = crearBotonMenu("Retirar Efectivo");
        JButton btnTransferir = crearBotonMenu("Transferir");
        JButton btnSalir = crearBotonMenu("Cerrar Sesión");
        btnSalir.setBackground(new Color(192, 57, 43));

        // --- CABLEADO DEL BOTÓN SALDO ---
        btnSaldo.addActionListener(e -> mostrarSaldo());

        // --- CABLEADO DEL BOTON RETIRAR---
        btnRetirar.addActionListener(e -> ejecutarRetiro());

        //---CABLEADO DEL BOTON TRANSFERIR---
        btnTransferir.addActionListener(e -> ejecutarTransferencia());

        // --- CABLEADO DEL BOTÓN SALIR ---
        btnSalir.addActionListener(e -> {
            new VentanaLogin().setVisible(true);
            this.dispose();
        });

        panelMenu.add(new JLabel("<html><font color='white'> ID Usuario: " + usuarioId, SwingConstants.CENTER));
        panelMenu.add(btnSaldo);
        panelMenu.add(btnRetirar);
        panelMenu.add(btnTransferir);
        panelMenu.add(btnSalir);

        add(panelMenu, BorderLayout.WEST);

        // Panel central de bienvenida
        JPanel panelInicio = new JPanel(new GridBagLayout());
        panelInicio.add(new JLabel("Bienvenido al sistema. Seleccione una operación."));
        add(panelInicio, BorderLayout.CENTER);
    }

    // Lógica para consultar el saldo
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
                sb.append(" 🏦 CUENTA: ").append(rs.getString("numero_cuenta")).append("\n");
                sb.append("    Tipo: ").append(rs.getString("tipo_cuenta")).append("\n");
                sb.append("    Saldo: $").append(String.format("%.2f", rs.getDouble("saldo"))).append("\n");
                sb.append(" --------------------------------------------\n\n");
            }

            if (tieneCuentas) {
                // 1. Creamos un JTextArea para contener el texto
                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false); // Que el usuario no pueda borrar el saldo
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Fuente tipo recibo
                textArea.setBackground(new Color(245, 245, 245));

                // 2. Lo metemos dentro de un JScrollPane (La magia del scroll)
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

    //=======================FUNCIONALIDAD PARA BOTON RETIRO=================


    // 2. Añade este método a la clase VentanaDashboard:
    private void ejecutarRetiro() {
        // Pedimos el número de cuenta
        String numCuenta = JOptionPane.showInputDialog(this, "Ingrese el número de cuenta:");
        if (numCuenta == null || numCuenta.isEmpty()) return;

        // Pedimos el monto
        String montoStr = JOptionPane.showInputDialog(this, "Monto a retirar:");
        if (montoStr == null || montoStr.isEmpty()) return;

        try {
            double monto = Double.parseDouble(montoStr);

            // Conexión y Lógica de Retiro
            try (Connection con = ConexionBD.obtenerConexion()) {
                con.setAutoCommit(false); // Iniciamos transacción

                // Verificamos saldo actual
                String sqlCheck = "SELECT saldo FROM cuentas WHERE numero_cuenta = ? AND usuario_id = ?";
                PreparedStatement psCheck = con.prepareStatement(sqlCheck);
                psCheck.setString(1, numCuenta);
                psCheck.setInt(2, usuarioId);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    double saldoActual = rs.getDouble("saldo");

                    // --- AQUÍ APLICAMOS TU REGLA DE NEGOCIO ---
                    if (saldoActual - monto < 10.0) {
                        JOptionPane.showMessageDialog(this,
                                "ALERTA: Operación rechazada. El saldo debe quedar al menos en $10.0",
                                "Regla de Negocio", JOptionPane.ERROR_MESSAGE);
                        con.rollback();
                        return;
                    }

                    // Ejecutamos el Update
                    String sqlUpdate = "UPDATE cuentas SET saldo = saldo - ? WHERE numero_cuenta = ?";
                    PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                    psUpdate.setDouble(1, monto);
                    psUpdate.setString(2, numCuenta);
                    psUpdate.executeUpdate();

                    // Registramos en Auditoría (lo que hicimos antes)
                    registrarAuditoria(con, "RETIRO EXITOSO", "Retiro de $" + monto + " en cuenta " + numCuenta);

                    con.commit(); // Guardamos cambios
                    JOptionPane.showMessageDialog(this, "Retiro exitoso. Nuevo saldo: $" + (saldoActual - monto));
                } else {
                    JOptionPane.showMessageDialog(this, "La cuenta no existe o no le pertenece.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un monto válido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error en la transacción: " + e.getMessage());
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

    //====================BOTON DE TRANSFERENCIA Y SU LOGICA======================
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
}