import javax.swing.*;
public static void main(String[] args) {
    // Para lanzar el formulario de insert y edicion , elim
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Reportes Clientes ");
        RegistroClientes rc = new RegistroClientes();
        frame.setContentPane(rc.getPanel()); // ← asumiendo método getPanel()
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Llamar la carga de datos en JTable
        rc.cargarClientsData();
    });
}