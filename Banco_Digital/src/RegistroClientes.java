import javax.swing.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroClientes {
    private JPanel Panel;
    private JTextField textField1; // Nombre
    private JTextField textField2; // Telefono
    private JTextField textField3; // Pais
    private JTextField textField4; // Correo
    private JTextField textField5; // Direccion
    private JButton eliminarButton;
    private JButton ACTUALIZARButton;
    private JButton INSERTARButton;
    private JTextField textField6; // ID del cliente (usado para eliminar o actualizar)
    private JTable TablaClientes;

    // Constructor
    public RegistroClientes() {
        TablaClientes.setRowHeight(25);

        // Asociar eventos a botones
        INSERTARButton.addActionListener(e -> insertarCliente());
        eliminarButton.addActionListener(e -> eliminarCliente());
        ACTUALIZARButton.addActionListener(e -> actualizarCliente());
    }

    // Ajusta automáticamente el ancho de las columnas del JTable
    public void ajustarAnchoColumnas(JTable tabla) {
        final javax.swing.table.TableColumnModel columnModel = tabla.getColumnModel();

        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            int anchoColumna = 200; // ancho mínimo
            javax.swing.table.TableColumn columnaTabla = columnModel.getColumn(columna);

            // Ancho del encabezado
            javax.swing.table.TableCellRenderer rendererHeader = columnaTabla.getHeaderRenderer();
            if (rendererHeader == null) {
                rendererHeader = tabla.getTableHeader().getDefaultRenderer();
            }
            Component compHeader = rendererHeader.getTableCellRendererComponent(tabla, columnaTabla.getHeaderValue(), false, false, 0, 0);
            anchoColumna = Math.max(compHeader.getPreferredSize().width, anchoColumna);

            // Ancho del contenido
            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                javax.swing.table.TableCellRenderer renderer = tabla.getCellRenderer(fila, columna);
                Component comp = tabla.prepareRenderer(renderer, fila, columna);
                anchoColumna = Math.max(comp.getPreferredSize().width, anchoColumna);
            }

            columnaTabla.setPreferredWidth(anchoColumna + 10);
        }
    }

    private void createUIComponents() {
        // Aquí iría el código de creación de componentes personalizados si se usaran
    }

    // Cargar los datos desde la base de datos al JTable
    public void cargarClientsData() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID_Cliente");
        model.addColumn("Nombre");
        model.addColumn("Correo");
        model.addColumn("Direccion");
        model.addColumn("Pais");
        model.addColumn("Telefono");

        String sql = "SELECT cliente_id, nombre, correo, Direccion, Pais, Telefono FROM Cliente";

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

                model.addRow(fila);
            }

            TablaClientes.setModel(model);
            ajustarAnchoColumnas(TablaClientes);
            TablaClientes.revalidate();
            TablaClientes.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar clientes: " + e.getMessage());
        }
    }

    // Getter para retornar el panel principal
    public JPanel getPanel() {
        return Panel;
    }

    // Inserta un nuevo cliente en la base de datos
    private void insertarCliente() {
        String nombre = textField1.getText();
        String correo = textField4.getText();
        String direccion = textField5.getText();
        String pais = textField3.getText();
        String telefono = textField2.getText();

        String sql = "INSERT INTO Cliente (nombre, correo, Direccion, Pais, Telefono) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, correo);
            pstmt.setString(3, direccion);
            pstmt.setString(4, pais);
            pstmt.setString(5, telefono);

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Cliente insertado correctamente.");
                cargarClientsData();
                limpiarCampos();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al insertar cliente: " + ex.getMessage());
        }
    }

    // Elimina un cliente por ID
    private void eliminarCliente() {
        String id = textField6.getText();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el ID del cliente a eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null, "¿Eliminar cliente con ID: " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM Cliente WHERE cliente_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Cliente eliminado correctamente.");
                cargarClientsData();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el cliente.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al eliminar cliente: " + ex.getMessage());
        }
    }

    // Actualiza los datos de un cliente por su ID
    private void actualizarCliente() {
        String id = textField6.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el ID del cliente a actualizar.");
            return;
        }

        String nombre = textField1.getText().trim();
        String correo = textField4.getText().trim();
        String direccion = textField5.getText().trim();
        String pais = textField3.getText().trim();
        String telefono = textField2.getText().trim();

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Obtener valores actuales
            String selectSQL = "SELECT * FROM Cliente WHERE cliente_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
            selectStmt.setString(1, id);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "No se encontró el cliente.");
                return;
            }

            // 2. Reemplazar solo lo que el usuario ingresó
            if (nombre.isEmpty()) nombre = rs.getString("nombre");
            if (correo.isEmpty()) correo = rs.getString("correo");
            if (direccion.isEmpty()) direccion = rs.getString("Direccion");
            if (pais.isEmpty()) pais = rs.getString("Pais");
            if (telefono.isEmpty()) telefono = rs.getString("Telefono");

            // 3. Ejecutar actualización con datos combinados
            String updateSQL = "UPDATE Cliente SET nombre = ?, correo = ?, Direccion = ?, Pais = ?, Telefono = ? WHERE cliente_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
            updateStmt.setString(1, nombre);
            updateStmt.setString(2, correo);
            updateStmt.setString(3, direccion);
            updateStmt.setString(4, pais);
            updateStmt.setString(5, telefono);
            updateStmt.setString(6, id);

            int filas = updateStmt.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Cliente actualizado correctamente.");
                cargarClientsData();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo actualizar el cliente.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al actualizar cliente: " + ex.getMessage());
        }
    }


    // Limpia todos los campos del formulario
    private void limpiarCampos() {
        textField1.setText("");
        textField2.setText("");
        textField3.setText("");
        textField4.setText("");
        textField5.setText("");
        textField6.setText(""); // ID también
    }
}
