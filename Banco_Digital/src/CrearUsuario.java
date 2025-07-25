import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Arrays;
import java.util.Base64;

public class CrearUsuario {
    private JPanel Pformularioinicio;
    private JTextField correo;
    private JTextField texnombre;
    private JTextField Direccion;
    private JTextField pais;
    private JTextField textField1;
    private JButton registra;
    private JLabel bienvenidreg;
    private JComboBox tipocuenta;
    private JTextField textField2;
    private JPasswordField campoPassword;
    public CrearUsuario() {
        // Acción del botón Registrar
        registra.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarNuevoUsuario();
            }
        });
    }

    // Método que realiza todo el proceso de registro
    private void registrarNuevoUsuario() {
        // Captura de datos del formulario
        String nombre = texnombre.getText().trim();
        String correoVal = correo.getText().trim();
        String direccionVal = Direccion.getText().trim();
        String paisVal = pais.getText().trim();
        String telefonoVal = textField1.getText().trim();

        String username = textField2.getText().trim();
        char[] passwordChars = campoPassword.getPassword();

        if (nombre.isEmpty() || correoVal.isEmpty() || username.isEmpty() || passwordChars.length == 0) {
            JOptionPane.showMessageDialog(Pformularioinicio, "Por favor completa todos los campos requeridos.");
            return;
        }

        String password = new String(passwordChars);
        int tipoCuentaId = tipocuenta.getSelectedIndex() + 1;  // Index desde 0

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);  // Inicia transacción

            // Insertar Cliente
            String insertCliente = "INSERT INTO Cliente(nombre, correo, direccion, pais, telefono) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psCliente = conn.prepareStatement(insertCliente, Statement.RETURN_GENERATED_KEYS);
            psCliente.setString(1, nombre);
            psCliente.setString(2, correoVal);
            psCliente.setString(3, direccionVal);
            psCliente.setString(4, paisVal);
            psCliente.setString(5, telefonoVal);
            psCliente.executeUpdate();

            ResultSet rsCliente = psCliente.getGeneratedKeys();
            int clienteId = 0;
            if (rsCliente.next()) {
                clienteId = rsCliente.getInt(1);
            }

            // Generar salt y hash
            String salt = generarSalt();
            byte[] passwordHash = hashPassword(password, salt);

            // Insertar Usuario
            String insertUsuario = "INSERT INTO Usuario(cliente_id, username, password_hash, salt) VALUES (?, ?, ?, ?)";
            PreparedStatement psUsuario = conn.prepareStatement(insertUsuario);
            psUsuario.setInt(1, clienteId);
            psUsuario.setString(2, username);
            psUsuario.setBytes(3, passwordHash);
            psUsuario.setString(4, salt);
            psUsuario.executeUpdate();

            // Insertar Cuenta
            String insertCuenta = "INSERT INTO Cuenta(cliente_id, tipo_cuenta_id, saldo, activa) VALUES (?, ?, 0, 1)";
            PreparedStatement psCuenta = conn.prepareStatement(insertCuenta);
            psCuenta.setInt(1, clienteId);
            psCuenta.setInt(2, tipoCuentaId);
            psCuenta.executeUpdate();

            conn.commit();  // Confirma todo

            JOptionPane.showMessageDialog(Pformularioinicio, "Usuario registrado exitosamente.");

            // Limpieza de la contraseña
            Arrays.fill(passwordChars, '0');

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Pformularioinicio, "Error al registrar: " + ex.getMessage());
        }
    }

    // Genera un salt aleatorio
    public static String generarSalt() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    // Aplica SHA-256 a la contraseña + salt
    public static byte[] hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            return md.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public JPanel getPformularioinicio() {
        return Pformularioinicio;
    }
}
