package com.bank.view;

import com.bank.model.Usuario;
import com.bank.service.BankService;
import com.bank.service.ReporteService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

@Component
public class VentanaLogin extends JFrame {

    private final BankService bankService;
    private final ReporteService reporteService;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public VentanaLogin(BankService bankService, ReporteService reporteService) {
        this.bankService = bankService;
        this.reporteService = reporteService;

        configurarVentana();
        inicializarComponentes();

        // Hacer que el botón de login responda a la tecla ENTER automáticamente
        this.getRootPane().setDefaultButton(btnLogin);
    }

    private void configurarVentana() {
        setTitle("JavaBank Pro - Acceso Seguro");
        setSize(450, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void inicializarComponentes() {
        // Panel con GridBagLayout para control total del diseño
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(28, 40, 51)); // Azul oscuro profundo
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Título ---
        JLabel lblTitulo = new JLabel("BANCA DIGITAL", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(236, 240, 241));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Autenticación de Usuario", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));
        gbc.gridy = 1;
        panel.add(lblSub, gbc);

        panel.add(Box.createVerticalStrut(20));

        // --- Campos ---
        gbc.gridwidth = 1;

        gbc.gridy = 2; gbc.gridx = 0;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setForeground(Color.WHITE);
        panel.add(lblUser, gbc);

        txtUsuario = new JTextField(15);
        estilizarCampo(txtUsuario);
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(Color.WHITE);
        panel.add(lblPass, gbc);

        txtPassword = new JPasswordField(15);
        estilizarCampo(txtPassword);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // --- Botón ---
        btnLogin = new JButton("INICIAR SESIÓN");
        btnLogin.setBackground(new Color(39, 174, 96)); // Verde esmeralda
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        btnLogin.addActionListener(e -> ejecutarLogin());

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 0, 10, 0);
        panel.add(btnLogin, gbc);

        add(panel);
    }

    private void estilizarCampo(JTextField campo) {
        campo.setBackground(new Color(44, 62, 80));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void ejecutarLogin() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Llamada al Service (Validación profesional con BCrypt interna)
        Optional<Usuario> usuarioOpt = bankService.login(username, password);

        if (usuarioOpt.isPresent()) {
            Usuario user = usuarioOpt.get();
            // Abrir Dashboard y cerrar Login
            new VentanaDashboard(bankService, reporteService, user).setVisible(true);
            this.dispose();
        } else {
            // REGISTRO DE SEGURIDAD (Fraud Attempt Alert) [cite: 2025-12-27]
            bankService.registrarLogSeguridad(username, "ACCESO_FALLIDO", "Intento de login con credenciales erróneas.");

            JOptionPane.showMessageDialog(this, "Credenciales incorrectas. El intento ha sido registrado.", "Alerta de Seguridad", JOptionPane.ERROR_MESSAGE);

            // Limpieza de seguridad
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}
