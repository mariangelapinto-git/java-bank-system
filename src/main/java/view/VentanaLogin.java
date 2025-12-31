package view;

import com.bank.util.ConexionBD;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentanaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public VentanaLogin() {
        setTitle("JavaBank - Acceso Seguro");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Fila 0: Título ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("INICIO DE SESIÓN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panelPrincipal.add(lblTitulo, gbc);

        // --- Fila 1: Usuario ---
        gbc.gridy = 1; gbc.gridwidth = 1;
        panelPrincipal.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        panelPrincipal.add(txtUsuario, gbc);

        // --- Fila 2: Contraseña ---
        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        panelPrincipal.add(txtPassword, gbc);

        // --- Fila 3: Botón ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        btnLogin = new JButton("Ingresar al Sistema");
        btnLogin.setPreferredSize(new Dimension(150, 35));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);

        // --- EL CABLEADO: Conecta el botón con el método ejecutarLogin ---
        btnLogin.addActionListener(e -> ejecutarLogin());

        panelPrincipal.add(btnLogin, gbc);
        add(panelPrincipal);
    }

    private void ejecutarLogin() {
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, llene todos los campos.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idObtenido = validarCredenciales(usuario, clave);

        if (idObtenido > 0) {
            JOptionPane.showMessageDialog(this, "¡Bienvenido al JavaBank!");
            abrirDashboard(idObtenido);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int validarCredenciales(String username, String password) {
        String sql = "SELECT id, password FROM usuarios WHERE username = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashBD = rs.getString("password");
                if (org.mindrot.jbcrypt.BCrypt.checkpw(password, hashBD)) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + e.getMessage());
        }
        return 0;
    }

    // --- NUEVO MÉTODO: Para abrir la ventana principal ---
    private void abrirDashboard(int idUsuario) {
        VentanaDashboard dashboard = new VentanaDashboard(idUsuario);
        dashboard.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
