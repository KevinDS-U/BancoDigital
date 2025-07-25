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
    private AbstractButton textFieldUsuario;
    private JPasswordField textFieldContrasena;

    public RegistroClientes() {
        TablaClientes.setRowHeight(25);

        // Asociar eventos a botones
        INSERTARButton.addActionListener(e -> insertarCliente());
        eliminarButton.addActionListener(e -> eliminarCliente());
        ACTUALIZARButton.addActionListener(e -> actualizarCliente());
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

    // Inserta un nuevo cliente en la base de datos
    void insertarCliente() {
        String nombre = textField1.getText();
        String correo = textField2.getText();
        String direccion = textField3.getText();
        String pais = textField4.getText();
        String telefono = textField5.getText();

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

            // 🔐 Insertar en la tabla Cuentas para el login
            String username = textFieldUsuario.getText();
            String password = new String(textFieldContrasena.getPassword());
            String passwordHash = HashUtil.hashPassword(password);

            String sqlCuenta = "INSERT INTO Cuentas (username, password_hash) VALUES (?, ?)";

            try (PreparedStatement psCuenta = conn.prepareStatement(sqlCuenta)) {
                psCuenta.setString(1, username);
                psCuenta.setString(2, passwordHash);
                psCuenta.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al crear cuenta: " + ex.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al insertar cliente: " + e.getMessage());
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
        String id = textField6.getText();
        String nombre = textField1.getText();
        String correo = textField2.getText();
        String direccion = textField3.getText();
        String pais = textField4.getText();
        String telefono = textField5.getText();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el ID del cliente a actualizar.");
            return;
        }

        String sql = "UPDATE Cliente SET nombre = ?, correo = ?, Direccion = ?, Pais = ?, Telefono = ? WHERE cliente_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, correo);
            pstmt.setString(3, direccion);
            pstmt.setString(4, pais);
            pstmt.setString(5, telefono);
            pstmt.setString(6, id);  // Faltaba este parámetro en tu versión original

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                JOptionPane.showMessageDialog(null, "Cliente actualizado correctamente.");
                cargarClientsData();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el cliente.");
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

