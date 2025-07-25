import javax.swing.*;
public class AppBancoDig {
/*
public static void main(String[] args) {
    // Para lanzar el formulario de insert y edicion , elim
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Reportes Clientes ");
        RegistroClientes rc = new RegistroClientes();
        frame.setContentPane(rc.getPanel()); // ← asumiendo metodo getPanel()
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);

        // Llamar la carga de datos en JTable
        rc.cargarClientsData();
    });
}*/
/*
public static void main(String[] args) {
    JFrame frame = new JFrame("Transferencia");
    TransferCuenta tc = new TransferCuenta(1,"Juan Perez");//prametro cliente_id , nombre
    frame.setContentPane(tc.getTRANFER());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}*/
// Ventana Principal
/*
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        // Simula cliente logueado con ID 1 y nombre "Kevin"
        PantallaPrincipal ventana = new PantallaPrincipal(1);//prametro cliente_id , nombre
        ventana.setVisible(true);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
    });
}
}*/
public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            JFrame frame = new JFrame("Registro de Usuario");
            CrearUsuario crearUsuario = new CrearUsuario();
            frame.setContentPane(crearUsuario.getPformularioinicio());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    });
}
}

