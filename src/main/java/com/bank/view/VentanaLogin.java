package com.bank.view;

import com.bank.model.Usuario;
import com.bank.service.BankService;
import com.bank.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

@Component
public class VentanaLogin extends JFrame {

    @Autowired
    private BankService bankService;

    @Autowired
    private ReporteService reporteService;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    public VentanaLogin() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("JavaBank - Acceso Seguro");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 240, 241));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título con Icono (Simulado con texto)
        JLabel lblTitulo = new JLabel("JAVABANK SYSTEM", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Usuario
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        txtUsuario = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Botón Ingresar
        JButton btnLogin = new JButton("INGRESAR");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogin.addActionListener(e -> ejecutarLogin());

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        add(panel);
    }

    private void ejecutarLogin() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }

        // Llamamos al service (Él se encarga de buscar en la BD y validar el BCrypt)
        Optional<Usuario> usuarioOpt = bankService.login(username, password);

        if (usuarioOpt.isPresent()) {
            Usuario user = usuarioOpt.get();
            JOptionPane.showMessageDialog(this, "Acceso concedido. Rol: " + user.getRol());

            // Pasamos los servicios y el usuario al Dashboard
            new VentanaDashboard(bankService, reporteService, user).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas.", "Error", JOptionPane.ERROR_MESSAGE);
            // Aquí podrías registrar un "Intento de Fraude" en los logs del sistema
        }
    }
}
