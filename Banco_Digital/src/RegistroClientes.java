import javax.swing.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
//Panel de Reporte de Clientes
public class RegistroClientes {
    private JPanel Panel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JButton eliminarButton;
    private JButton ACTUALIZARButton;
    private JButton INSERTARButton;
    private JTextField textField6;
    private JTable TablaClientes;

    public RegistroClientes() {
        TablaClientes.setRowHeight(25);
    }
    public void ajustarAnchoColumnas(JTable tabla) {
        final javax.swing.table.TableColumnModel columnModel = tabla.getColumnModel();

        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            int anchoColumna = 200; // ancho mínimo
            javax.swing.table.TableColumn columnaTabla = columnModel.getColumn(columna);

            // Ancho del header
            javax.swing.table.TableCellRenderer rendererHeader = columnaTabla.getHeaderRenderer();
            if (rendererHeader == null) {
                rendererHeader = tabla.getTableHeader().getDefaultRenderer();
            }
            Component compHeader = rendererHeader.getTableCellRendererComponent(tabla, columnaTabla.getHeaderValue(), false, false, 0, 0);
            anchoColumna = Math.max(compHeader.getPreferredSize().width, anchoColumna);

            // Ancho máximo del contenido de la columna
            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                javax.swing.table.TableCellRenderer renderer = tabla.getCellRenderer(fila, columna);
                Component comp = tabla.prepareRenderer(renderer, fila, columna);
                anchoColumna = Math.max(comp.getPreferredSize().width, anchoColumna);
            }

            // Ajuste final con margen
            columnaTabla.setPreferredWidth(anchoColumna + 10);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
    public void cargarClientsData(){
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID_Cliente");
        model.addColumn("Nombre");
        model.addColumn("Correo");
        model.addColumn("Direccion");
        model.addColumn("Pais");
        model.addColumn("Telefono");
        String sql = "SELECT cliente_id ,nombre ,correo, Direccion , Pais, Telefono From Cliente";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = new Object[6];
                fila[0] = rs.getString("cliente_id");
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getString("correo");
                fila[3] = rs.getString("Direccion");
                fila[4] = rs.getString("Pais");
                fila[5] = rs.getString("Telefono");

                model.addRow(fila);  // Agregar la fila al modelo
            }

            // Asignar modelo a tu JTable
            TablaClientes.setModel(model);
            ajustarAnchoColumnas(TablaClientes);
            TablaClientes.revalidate();
            TablaClientes.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar materias: " + e.getMessage());
        }
    }


    public JPanel getPanel() {
        return Panel;
    }
}
