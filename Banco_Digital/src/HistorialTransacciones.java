import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class HistorialTransacciones {
    private JPanel fondo;
    private JTable tablaHistorial;
    private JPanel LiniaArriba;
    private JLabel inicio;
    private JButton Historial;
    private JButton salirButton;
    private JButton Transa;

    public HistorialTransacciones(int clienteId) {
        cargarHistorial(clienteId);
    }

    private void cargarHistorial(int clienteId) {
        String[] columnas = {"Fecha", "Tipo", "Monto Total", "Descripción", "Número Cuenta", "Débito", "Crédito", "Fecha Movimiento"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        String query = """
            SELECT
                t.fecha,
                t.tipo,
                t.monto,
                t.descripcion,
                c.numero_cuenta,
                m.monto_debito,
                m.monto_credito,
                m.fecha AS fecha_movimiento
            FROM Transaccion t
            JOIN Movimiento m ON t.transaccion_id = m.transaccion_id
            JOIN Cuenta c ON m.cuenta_id = c.cuenta_id
            WHERE c.cliente_id = ?
            ORDER BY t.fecha DESC;
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String fecha = rs.getString("fecha");
                String tipo = rs.getString("tipo");
                double montoTotal = rs.getDouble("monto");
                String descripcion = rs.getString("descripcion");
                String numeroCuenta = rs.getString("numero_cuenta");
                double debito = rs.getDouble("monto_debito");
                double credito = rs.getDouble("monto_credito");
                String fechaMovimiento = rs.getString("fecha_movimiento");

                modelo.addRow(new Object[]{fecha, tipo, montoTotal, descripcion, numeroCuenta, debito, credito, fechaMovimiento});
            }

            tablaHistorial.setModel(modelo);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error cargando historial: " + e.getMessage());
        }
    }

    public JPanel getFondo() {
        return fondo;
    }
}


