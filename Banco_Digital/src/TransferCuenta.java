import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.PreparedStatement;
public class TransferCuenta {
    private JPanel TRANFER;
    private JComboBox<String> TipoTrans;
    private JLabel tex1;
    private JLabel tex2;
    private JComboBox<String> comboBox1;  // Cuenta Origen
    private JLabel origen;
    private JComboBox<String> comboBox2;  // Cuenta Destino (solo entre mis cuentas)
    private JLabel Destino;
    private JTextField monto;
    private JLabel labelmonto;
    private JTextField saldorigen;
    private JLabel NumCuentads;
    private JTextField numcd;             // Número cuenta destino (terceros)
    private JButton enviarBoton;
    private void createUIComponents() {
        saldorigen = new JTextField();
        saldorigen.setEditable(false);  // Ejemplo: solo lectura
        saldorigen.setBackground(new java.awt.Color(240, 240, 240)); // Color claro
    }
    public TransferCuenta() {
        cargarCuentas();      // Implementa conexión y carga real
        mostrarCamposIniciales();

        TipoTrans.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipo = (String) TipoTrans.getSelectedItem();
                limpiarCampos();

                if ("Entre mis Cuentas".equals(tipo)) {
                    mostrarEntreMisCuentas();
                } else if ("A Terceros".equals(tipo)) {
                    mostrarATerceros();
                } else {
                    mostrarCamposIniciales();
                }

                TRANFER.revalidate();
                TRANFER.repaint();
            }
        });

        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cuenta = (String) comboBox1.getSelectedItem();
                if (cuenta != null) {
                    cargarSaldoCuenta(cuenta);  // Implementa conexión y consulta real
                } else {
                    saldorigen.setText("");
                }
            }
        });

        enviarBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipo = (String) TipoTrans.getSelectedItem();
                String cuentaOrigen = (String) comboBox1.getSelectedItem();
                String cuentaDestino = (String) comboBox2.getSelectedItem();
                String numCuentaDestino = numcd.getText();
                double montoTrans;

                try {
                    montoTrans = Double.parseDouble(monto.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Ingrese un monto válido.");
                    return;
                }

                if (cuentaOrigen == null || cuentaOrigen.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Seleccione una cuenta de origen.");
                    return;
                }

                if ("Entre mis Cuentas".equals(tipo) && (cuentaDestino == null || cuentaDestino.isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Seleccione una cuenta de destino.");
                    return;
                }

                if ("A Terceros".equals(tipo) && (numCuentaDestino == null || numCuentaDestino.isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Ingrese el número de cuenta destino.");
                    return;
                }

                ejecutarTransferencia(tipo, cuentaOrigen, cuentaDestino, numCuentaDestino, montoTrans);
            }
        });
    }

    private void mostrarCamposIniciales() {
        comboBox1.setVisible(true);
        origen.setVisible(true);
        saldorigen.setVisible(true);

        comboBox2.setVisible(false);
        Destino.setVisible(false);
        numcd.setVisible(false);
        NumCuentads.setVisible(false);

        labelmonto.setVisible(true);
        monto.setVisible(true);
    }

    private void mostrarEntreMisCuentas() {
        comboBox2.setVisible(true);
        Destino.setVisible(true);
        numcd.setVisible(false);
        NumCuentads.setVisible(false);
    }

    private void mostrarATerceros() {
        comboBox2.setVisible(false);
        Destino.setVisible(false);
        numcd.setVisible(true);
        NumCuentads.setVisible(true);
    }

    private void limpiarCampos() {
        monto.setText("");
        numcd.setText("");
        saldorigen.setText("");

        comboBox1.setSelectedIndex(-1);
        comboBox2.setSelectedIndex(-1);
    }

    private void cargarCuentas() {
        comboBox1.removeAllItems();
        comboBox2.removeAllItems();

        // TODO: conecta a tu BD y carga las cuentas aquí, por ejemplo:

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT cuenta_id FROM Cuentas WHERE cliente_id = ?")) {

             while (rs.next()) {
                 String cuenta = rs.getString("cuenta_id");
                 comboBox1.addItem(cuenta);
                 comboBox2.addItem(cuenta);
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error cargando cuentas: " + e.getMessage());
        }

    }

    private void cargarSaldoCuenta(String cuenta) {
        // TODO: conecta a BD y consulta saldo de la cuenta para mostrar

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT saldo FROM Cuentas WHERE cuenta_id = ?")) {
            ps.setString(1, cuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double saldo = rs.getDouble("saldo");
                    saldorigen.setText(String.format("%.2f", saldo));
                } else {
                    saldorigen.setText("0.00");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error cargando saldo: " + e.getMessage());
        }

        saldorigen.setText("");  // Por defecto vacío mientras implementas
    }

    private void ejecutarTransferencia(String tipoTrans, String cuentaOrigen, String cuentaDestino, String numCuentaDestino, double monto) {
        String spName;

        if ("Entre mis Cuentas".equals(tipoTrans)) {
            spName = "{call sp_TransferenciaInterna(?, ?, ?)}";
        } else if ("A Terceros".equals(tipoTrans)) {
            spName = "{call sp_TransferenciaExterna(?, ?, ?)}";
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione un tipo válido de transferencia.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:tu_driver:tu_bd_url", "usuario", "password");
             CallableStatement cs = conn.prepareCall(spName)) {

            cs.setString(1, cuentaOrigen);

            if ("Entre mis Cuentas".equals(tipoTrans)) {
                cs.setString(2, cuentaDestino);
                cs.setDouble(3, monto);
            } else {
                cs.setString(2, numCuentaDestino);
                cs.setDouble(3, monto);
            }

            cs.execute();
            JOptionPane.showMessageDialog(null, "Transferencia realizada correctamente.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al realizar la transferencia: " + ex.getMessage());
        }
    }

    public JPanel getTRANFER() {
        return TRANFER;
    }

}

