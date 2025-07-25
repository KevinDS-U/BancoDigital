import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private DefaultTableModel tableModel;

    public LoginForm() {
        setTitle("Login Banco Dinamo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Panel de login
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Usuario:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton loginBtn = new JButton("Iniciar sesión");
        loginPanel.add(new JLabel()); // Espacio vacío
        loginPanel.add(loginBtn);

        add(loginPanel, BorderLayout.NORTH);

        // Tabla para mostrar cuentas del cliente
        String[] columnas = {"Tipo de Cuenta", "Número", "Saldo"};
        tableModel = new DefaultTableModel(columnas, 0);
        JTable cuentasTable = new JTable(tableModel);
        add(new JScrollPane(cuentasTable), BorderLayout.CENTER);

        // Acción del botón
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                String resultado = AuthService.login(username, password);
                JOptionPane.showMessageDialog(LoginForm.this, resultado);

                if ("Login exitoso".equals(resultado)) {
                    cargarCuentasDelCliente(username);
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void cargarCuentasDelCliente(String username) {
        tableModel.setRowCount(0); // Limpiar tabla

        try {
            Connection conn = DBConnection.getConnection();
            String sql = """
                SELECT 
                    tc.descripcion AS tipo_cuenta,
                    c.numero_cuenta,
                    c.saldo
                FROM Usuario u
                JOIN Cliente cl ON u.cliente_id = cl.cliente_id
                JOIN Cuenta c ON cl.cliente_id = c.cliente_id
                JOIN TipoCuenta tc ON c.tipo_cuenta_id = tc.tipo_cuenta_id
                WHERE u.username = ? AND c.activa = 1
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo_cuenta");
                String numero = rs.getString("numero_cuenta");
                double saldo = rs.getDouble("saldo");

                tableModel.addRow(new Object[]{tipo, numero, saldo});
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener cuentas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
