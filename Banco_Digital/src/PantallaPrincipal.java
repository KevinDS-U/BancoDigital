import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PantallaPrincipal extends JFrame {
    private JPanel LiniaArriba;
    private JTable Cuentas;
    private JPanel abajorelleno;
    private JLabel inicio;
    private JButton Transa;
    private JButton Historial;
    private JButton salirButton;
    private JLabel texbievenida;
    private JLabel miscuenta;
    private JLabel bievenida;
    private JPanel Pricipal0;

    public PantallaPrincipal(int cliente_Id) {
        setContentPane(Pricipal0);
        cargarDatosCuenta(cliente_Id);  // carga datos reales
        // Acción al botón salir
        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int opcion = JOptionPane.showConfirmDialog(null,
                        "¿Seguro que deseas salir?", "Confirmar salida",
                        JOptionPane.YES_NO_OPTION);
                if (opcion == JOptionPane.YES_OPTION) {
                    dispose();  // Cierra la ventana
                }
            }
        });
        Transa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombreUsuario = "Juan Perez";
                JFrame ventanaActual = (JFrame) SwingUtilities.getWindowAncestor(Pricipal0);
                ventanaActual.setVisible(false);  // Oculta la ventana principal

                TransferCuenta tc = new TransferCuenta(cliente_Id, nombreUsuario, ventanaActual); // PASAR la ventana
                JFrame frame = new JFrame("Transferencia");
                frame.setContentPane(tc.getTRANFER());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });

        Historial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistorialTransacciones ht = new HistorialTransacciones(cliente_Id);
                JFrame frame = new JFrame("Historial de Transacciones"); // Crea un nuevo frame para mostrar la pantalla
                frame.setContentPane(ht.getFondo());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null); // centrar
                frame.setVisible(true);
            }
        });
    }

    private void cargarDatosCuenta(int clienteId) {
        String[] columnas = {"Número Cuenta", "Tipo", "Saldo"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        String query = """
        SELECT c.numero_cuenta, tc.descripcion AS tipo, c.saldo
        FROM Cuenta c
        JOIN TipoCuenta tc ON c.tipo_cuenta_id = tc.tipo_cuenta_id
        WHERE c.cliente_id = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String numero = rs.getString("numero_cuenta");
                String tipo = rs.getString("tipo");
                double saldo = rs.getDouble("saldo");

                modelo.addRow(new Object[]{numero, tipo, String.format("%.2f", saldo)});
            }

            Cuentas.setModel(modelo);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error cargando cuentas: " + e.getMessage());
        }
    }
}
