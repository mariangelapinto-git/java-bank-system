package com.bank.view;

import com.bank.exception.BankException;
import com.bank.model.*;
import com.bank.service.BankService;
import com.bank.service.ReporteService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaDashboard extends JFrame {

    private final BankService bankService;
    private final ReporteService reporteService;
    private final Usuario usuarioLogueado;
    private JPanel panelContenido;

    public VentanaDashboard(BankService bankService, ReporteService reporteService, Usuario usuario) {
        this.bankService = bankService;
        this.reporteService = reporteService;
        this.usuarioLogueado = usuario;

        configurarVentana();

        // --- SIDEBAR (Menú Lateral) ---
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBackground(new Color(28, 40, 51));
        panelMenu.setPreferredSize(new Dimension(280, 750));
        panelMenu.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblTitulo = new JLabel("JAVA BANK PRO");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 25, 0));
        panelMenu.add(lblTitulo);

        // --- GRUPO 1: GESTIÓN DE CUENTAS ---
        agregarBotonMenu(panelMenu, "  Abrir Cuenta", e -> ejecutarNuevaCuenta());
        agregarBotonMenu(panelMenu, "  Listar Cuentas", e -> mostrarListadoCuentas());
        agregarBotonMenu(panelMenu, "  Cerrar Cuenta", e -> ejecutarCerrarCuenta());
        panelMenu.add(Box.createVerticalStrut(15));

        // --- GRUPO 2: TRANSACCIONES ---
        agregarBotonMenu(panelMenu, "  Ver Saldo", e -> consultarSaldo());
        agregarBotonMenu(panelMenu, "  Depósito", e -> ejecutarDeposito());
        agregarBotonMenu(panelMenu, "  Retiro", e -> ejecutarRetiro());
        agregarBotonMenu(panelMenu, "  Transferir", e -> ejecutarTransferencia());
        panelMenu.add(Box.createVerticalStrut(15));

        // --- GRUPO 3: SERVICIOS AVANZADOS ---
        agregarBotonMenu(panelMenu, "  Generar PDF", e -> ejecutarReportePDF());
        agregarBotonMenu(panelMenu, "️  Cierre Mes", e -> ejecutarCierreMes());
        agregarBotonMenu(panelMenu, "  Préstamos", e -> evaluarPrestamos());

        panelMenu.add(Box.createVerticalGlue());

        // --- SALIR ---
        JButton btnSalir = agregarBotonMenu(panelMenu, "   Salir", e -> this.dispose());
        btnSalir.setBackground(new Color(192, 57, 43));

        add(panelMenu, BorderLayout.WEST);

        // --- PANEL CENTRAL ---
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(new Color(244, 246, 247));
        mostrarPantallaBienvenida();
        add(panelContenido, BorderLayout.CENTER);
    }

    private void configurarVentana() {
        setTitle("JavaBank Pro - Panel de Control");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void mostrarPantallaBienvenida() {
        panelContenido.removeAll();
        JLabel lbl = new JLabel("Bienvenid@, " + usuarioLogueado.getUsername(), SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setForeground(new Color(52, 73, 94));
        panelContenido.add(lbl, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private JButton agregarBotonMenu(JPanel panel, String texto, java.awt.event.ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(44, 62, 80));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(accion);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(8));
        return btn;
    }

    // --- MÉTODOS DE LÓGICA ---

    private void consultarSaldo() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "ID de cuenta para ver saldo:");
            if (idStr != null) {
                double saldo = bankService.obtenerSaldo(Long.parseLong(idStr));
                JOptionPane.showMessageDialog(this, "Saldo Actual: $" + String.format("%.2f", saldo));
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void ejecutarCerrarCuenta() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "ID de cuenta a CERRAR:");
            if (idStr != null) {
                bankService.cambiarEstadoCuenta(Long.parseLong(idStr), "CERRADA");
                JOptionPane.showMessageDialog(this, "Cuenta cerrada con éxito.");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void ejecutarNuevaCuenta() {
        String[] opciones = {"Ahorros", "Corriente", "Nomina"};
        String tipo = (String) JOptionPane.showInputDialog(this, "Tipo de cuenta:", "Nueva Cuenta",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (tipo == null) return;

        try {
            String inputMonto = JOptionPane.showInputDialog(this, "Monto de apertura ($10.0 min):");
            if (inputMonto == null) return;
            double montoInicial = Double.parseDouble(inputMonto);
            String numCuenta = "CTA-" + (int)(Math.random() * 900000 + 100000);

            Cuenta nueva;
            if (tipo.equals("Ahorros")) nueva = new CuentaAhorro(numCuenta, usuarioLogueado.getUsername(), "N/A", "N/A", "N/A", montoInicial);
            else if (tipo.equals("Corriente")) nueva = new CuentaCorriente(numCuenta, usuarioLogueado.getUsername(), "N/A", "N/A", "N/A", montoInicial);
            else nueva = new CuentaNomina(numCuenta, usuarioLogueado.getUsername(), "N/A", "N/A", "N/A", montoInicial);

            nueva.setUsuario(usuarioLogueado);
            bankService.crearCuenta(nueva, usuarioLogueado.getId());
            JOptionPane.showMessageDialog(this, "¡Éxito! Cuenta " + tipo + " abierta.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void mostrarListadoCuentas() {
        String[] columnas = {"ID", "Cuenta #", "Tipo", "Saldo", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        List<Cuenta> cuentas = bankService.obtenerTodasLasCuentas();
        for (Cuenta c : cuentas) {
            Object[] fila = {c.getId(), c.getNumeroCuenta(), c.getClass().getSimpleName().replace("Cuenta", ""), String.format("$%.2f", c.getSaldo()), c.getEstado()};
            modelo.addRow(fila);
        }
        JTable tabla = new JTable(modelo);
        JOptionPane.showMessageDialog(this, new JScrollPane(tabla), "Mis Cuentas", JOptionPane.PLAIN_MESSAGE);
    }

    private void ejecutarDeposito() {
        try {
            String idInput = JOptionPane.showInputDialog("ID de la cuenta:");
            if (idInput == null) return;
            bankService.depositar(Long.parseLong(idInput), Double.parseDouble(JOptionPane.showInputDialog("Monto:")));
            JOptionPane.showMessageDialog(this, "Depósito exitoso.");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void ejecutarRetiro() {
        try {
            String idInput = JOptionPane.showInputDialog(this, "ID de la cuenta:");
            if (idInput == null) return;
            bankService.retirar(Long.parseLong(idInput), Double.parseDouble(JOptionPane.showInputDialog("Monto:")), usuarioLogueado.getId());
            JOptionPane.showMessageDialog(this, "Retiro exitoso.");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void ejecutarTransferencia() {
        try {
            String idOrigenStr = JOptionPane.showInputDialog(this, "ID de Cuenta Origen:");
            if (idOrigenStr == null) return;

            String numCuentaDestino = JOptionPane.showInputDialog(this, "Número de Cuenta Destino (String):");
            if (numCuentaDestino == null) return;

            String montoStr = JOptionPane.showInputDialog(this, "Monto a transferir:");
            if (montoStr == null) return;

            bankService.transferir(Long.parseLong(idOrigenStr), numCuentaDestino, Double.parseDouble(montoStr), usuarioLogueado.getId());
            JOptionPane.showMessageDialog(this, "✅ Transferencia procesada exitosamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en transferencia: " + e.getMessage());
        }
    }

    private void ejecutarReportePDF() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "ID de cuenta para reporte:");
            if (idStr != null) {
                reporteService.generarEstadoCuenta(Long.parseLong(idStr));
                JOptionPane.showMessageDialog(this, "PDF generado con éxito.");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error PDF: " + e.getMessage()); }
    }

    private void ejecutarCierreMes() {
        if (JOptionPane.showConfirmDialog(this, "¿Ejecutar intereses?") == JOptionPane.YES_OPTION) {
            bankService.ejecutarInteresesBatch();
            JOptionPane.showMessageDialog(this, "Proceso completado.");
        }
    }

    private void evaluarPrestamos() {
        try {
            String idStr = JOptionPane.showInputDialog("ID para análisis de préstamo:");
            if (idStr != null) {
                String resultado = bankService.evaluarPrestamo(Long.parseLong(idStr));
                JOptionPane.showMessageDialog(this, resultado);
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}