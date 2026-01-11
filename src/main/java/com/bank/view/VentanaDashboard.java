package com.bank.view;

import com.bank.exception.BankException;
import com.bank.model.*;
import com.bank.service.BankService;
import com.bank.service.ReporteService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaDashboard extends JFrame {

    private final BankService bankService;
    private final ReporteService reporteService;
    private final Usuario usuarioLogueado;

    public VentanaDashboard(BankService bankService, ReporteService reporteService, Usuario usuario) {
        this.bankService = bankService;
        this.reporteService = reporteService;
        this.usuarioLogueado = usuario;

        setTitle("JavaBank - Panel de Control: " + usuario.getUsername());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Panel Lateral ---
        JPanel panelMenu = new JPanel(new GridLayout(0, 1, 5, 5));
        panelMenu.setBackground(new Color(44, 62, 80));
        panelMenu.setPreferredSize(new Dimension(250, 600));

        panelMenu.add(new JLabel("<html><font color='white' size='5'><b>&nbsp;OPERACIONES</b>", SwingConstants.LEFT));

        agregarBoton(panelMenu, "Abrir Cuenta Nueva", e -> ejecutarNuevaCuenta());
        agregarBoton(panelMenu, "Ver Mis Cuentas", e -> mostrarListadoCuentas());
        agregarBoton(panelMenu, "Depósito de Dinero", e -> ejecutarDeposito());
        agregarBoton(panelMenu, "Retirar Efectivo", e -> ejecutarRetiro());
        agregarBoton(panelMenu, "Generar Reporte PDF", e -> ejecutarReportePDF());
        agregarBoton(panelMenu, "Evaluar Préstamos", e -> evaluarPrestamos());

        if ("ADMIN".equals(usuario.getRol())) {
            JButton btnAdmin = agregarBoton(panelMenu, "Panel Admin (Logs)", e -> mostrarPanelAdmin());
            btnAdmin.setBackground(new Color(39, 174, 96));
            JButton btnCierre = agregarBoton(panelMenu, "Cierre de Mes", e -> ejecutarCierreMes());
            btnCierre.setBackground(new Color(39, 174, 96));
        }

        JButton btnSalir = agregarBoton(panelMenu, "Cerrar Sesión", e -> {
            this.dispose();
        });
        btnSalir.setBackground(new Color(192, 57, 43));

        add(panelMenu, BorderLayout.WEST);

        JPanel panelInicio = new JPanel(new GridBagLayout());
        JLabel lblUser = new JLabel("Bienvenid@, " + usuario.getUsername() + " [" + usuario.getRol() + "]");
        lblUser.setFont(new Font("Arial", Font.BOLD, 16));
        panelInicio.add(lblUser);
        add(panelInicio, BorderLayout.CENTER);
    }

    private JButton agregarBoton(JPanel panel, String texto, java.awt.event.ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.addActionListener(accion);
        panel.add(btn);
        return btn;
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
            // 1. Usamos Polimorfismo e Herencia según tus notas [cite: 2025-12-27]
            if (tipo.equals("Ahorros")) {
                nueva = new CuentaAhorro(numCuenta, usuarioLogueado.getUsername(), "N/A", "N/A", "N/A", montoInicial);
            } else if (tipo.equals("Corriente")) {
                nueva = new CuentaCorriente(numCuenta, usuarioLogueado.getUsername(), "N/A", "N/A", "N/A", montoInicial);
            } else {
                nueva = new CuentaNomina(numCuenta, usuarioLogueado.getUsername(), "N/A", "N/A", "N/A", montoInicial);
            }

            nueva.setUsuario(usuarioLogueado);

            bankService.crearCuenta(nueva, usuarioLogueado.getId());

            JOptionPane.showMessageDialog(this, "¡Éxito! Cuenta " + tipo + " abierta y vinculada a su usuario.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: Ingrese un monto numérico válido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al crear cuenta: " + e.getMessage());
        }
    }

    private void mostrarListadoCuentas() {
        String[] columnas = {"ID", "Cuenta #", "Tipo", "Saldo", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        List<Cuenta> cuentas = bankService.obtenerTodasLasCuentas();
        for (Cuenta c : cuentas) {
            Object[] fila = {
                    c.getId(),
                    c.getNumeroCuenta(),
                    c.getClass().getSimpleName().replace("Cuenta", ""),
                    String.format("$%.2f", c.getSaldo()),
                    c.getEstado()
            };
            modelo.addRow(fila);
        }
        JTable tabla = new JTable(modelo);
        JOptionPane.showMessageDialog(this, new JScrollPane(tabla), "Mis Cuentas", JOptionPane.PLAIN_MESSAGE);
    }

    private void ejecutarDeposito() {
        try {
            String idInput = JOptionPane.showInputDialog("ID de la cuenta:");
            if (idInput == null) return;
            Long id = Long.parseLong(idInput);

            String montoInput = JOptionPane.showInputDialog("Monto a depositar:");
            if (montoInput == null) return;
            double monto = Double.parseDouble(montoInput);

            bankService.depositar(id, monto);
            JOptionPane.showMessageDialog(this, "Depósito exitoso.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void ejecutarRetiro() {
        try {
            String idInput = JOptionPane.showInputDialog(this, "ID de la cuenta:");
            if (idInput == null) return;
            Long idCuenta = Long.parseLong(idInput);

            String montoInput = JOptionPane.showInputDialog(this, "Monto a retirar:");
            if (montoInput == null) return;
            double monto = Double.parseDouble(montoInput);

            bankService.retirar(idCuenta, monto, usuarioLogueado.getId());
            JOptionPane.showMessageDialog(this, "Retiro procesado exitosamente.");
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Alerta de Banco", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Entrada inválida.");
        }
    }

    private void mostrarPanelAdmin() {
        List<String> logs = bankService.obtenerLogsRecientes();
        StringBuilder sb = new StringBuilder("--- ÚLTIMOS MOVIMIENTOS ---\n\n");
        logs.forEach(log -> sb.append(log).append("\n"));

        JTextArea areaTexto = new JTextArea(sb.toString(), 15, 60);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setEditable(false);

        Object[] opciones = {"Reactivar Cuenta", "Cerrar"};
        int seleccion = JOptionPane.showOptionDialog(this, new JScrollPane(areaTexto), "Panel de Auditoría", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[1]);

        if (seleccion == JOptionPane.YES_OPTION) {
            try {
                String idInput = JOptionPane.showInputDialog(this, "ID de cuenta a REACTIVAR:");
                if (idInput != null) {
                    Long id = Long.parseLong(idInput);
                    bankService.cambiarEstadoCuenta(id, "ACTIVO");
                    JOptionPane.showMessageDialog(this, "Cuenta reactivada.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: ID inválido.");
            }
        }
    }

    private void ejecutarReportePDF() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "Ingrese ID de cuenta:");
            if (idStr != null) {
                reporteService.generarEstadoCuenta(Long.parseLong(idStr));
                JOptionPane.showMessageDialog(this, "PDF generado.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error PDF: " + e.getMessage());
        }
    }

    private void ejecutarCierreMes() {
        if (JOptionPane.showConfirmDialog(this, "¿Ejecutar cierre de mes?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            bankService.ejecutarInteresesBatch();
            JOptionPane.showMessageDialog(this, "Proceso completado.");
        }
    }

    private void evaluarPrestamos() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "ID de cuenta para análisis:");
            if (idStr != null) {
                String resultado = bankService.evaluarPrestamo(Long.parseLong(idStr));
                JOptionPane.showMessageDialog(this, resultado, "Análisis Crediticio", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error evaluación: " + e.getMessage());
        }
    }
}