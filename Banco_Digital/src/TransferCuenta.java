import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

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
    private JTextField numcd;
    private int cliente_id;// Número cuenta destino
    private JButton enviarBoton;
    private JLabel saldd;

    private void createUIComponents() {
        saldorigen = new JTextField();
        saldorigen.setEditable(false);  // Ejemplo: solo lectura
        saldorigen.setBackground(new java.awt.Color(240, 240, 240)); // Color claro
    }
    public TransferCuenta(int cliente_id) {
        this.cliente_id=cliente_id;
        cargarCuentasDeCliente(cliente_id);      // Implementa conexión y carga real
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
                      String cuentaStr = (String) comboBox1.getSelectedItem();

                      if (cuentaStr != null) {
                          try {
                              int cuentaId = Integer.parseInt(cuentaStr);  // convierte a int
                              cargarSaldoCuenta(cuentaId);  // ahora llamamos con int
                          } catch (NumberFormatException ex) {
                              JOptionPane.showMessageDialog(null, "Cuenta inválida: " + ex.getMessage());
                          }
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
        comboBox1.setVisible(false);
        origen.setVisible(false);
        saldorigen.setVisible(false);

        comboBox2.setVisible(false);
        Destino.setVisible(false);
        numcd.setVisible(false);
        NumCuentads.setVisible(false);

        labelmonto.setVisible(false);
        monto.setVisible(false);
        saldd.setVisible(false);
        enviarBoton.setVisible(false);
    }

    private void mostrarEntreMisCuentas() {
        comboBox1.setVisible(true);
        comboBox2.setVisible(true);
        origen.setVisible(true);
        Destino.setVisible(true);
        numcd.setVisible(false);
        saldorigen.setVisible(true);
        NumCuentads.setVisible(false);
        saldd.setVisible(true);
        labelmonto.setVisible(true);
        monto.setVisible(true);
        enviarBoton.setVisible(true);
        cargarCuentasSoloOrigen(cliente_id); 
    }

    private void mostrarATerceros() {
        comboBox2.setVisible(false);
        Destino.setVisible(false);
        numcd.setVisible(true);
        origen.setVisible(true);
        NumCuentads.setVisible(true);
        cargarCuentasSoloOrigen(cliente_id);
        comboBox1.setVisible(true);
        saldorigen.setVisible(true);
        saldd.setVisible(true);
        labelmonto.setVisible(true);
        monto.setVisible(true);
        enviarBoton.setVisible(true);
        cargarCuentasSoloOrigen(cliente_id);
    }



    private void cargarCuentasSoloOrigen(int cliente_id) {
           comboBox1.removeAllItems();  // Limpiar primero

           String query = "SELECT numero_cuenta FROM Cuenta WHERE cliente_id = ?";
           try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

               ps.setInt(1, cliente_id);
               ResultSet rs = ps.executeQuery();
               while (rs.next()) {
                   String numeroCuenta = rs.getString("numero_cuenta");
                   comboBox1.addItem(numeroCuenta);  // Agregas solo cuenta de origen
               }

           } catch (SQLException e) {
               JOptionPane.showMessageDialog(null, "Error cargando cuentas: " + e.getMessage());
    }      }

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
             ResultSet rs = stmt.executeQuery("SELECT cuenta_id FROM Cuenta WHERE cliente_id = 1")) {

             while (rs.next()) {
                 String cuenta = rs.getString("cuenta_id");
                 comboBox1.addItem(cuenta);
                 comboBox2.addItem(cuenta);
             }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error cargando cuentas: " + e.getMessage());
        }

    }

      private void cargarCuentasDeCliente(int cliente_id) {
          comboBox1.removeAllItems();
          comboBox2.removeAllItems();

          List<String> cuentas = new ArrayList<>();
          String query = "SELECT numero_cuenta FROM cuenta WHERE cliente_id = ?";
          try (Connection conn = DBConnection.getConnection();
               PreparedStatement ps = conn.prepareStatement(query)) {

              ps.setInt(1, cliente_id);
              ResultSet rs = ps.executeQuery();
              while (rs.next()) {
                  cuentas.add(rs.getString("numero_cuenta"));
              }

          } catch (SQLException e) {
              JOptionPane.showMessageDialog(null, "Error cargando cuentas: " + e.getMessage());
              return;
          }

          if (cuentas.size() < 2) {
              JOptionPane.showMessageDialog(null, "Debe tener al menos 2 cuentas para esta transferencia.");
              return;
          }

          for (String cuenta : cuentas) {
              comboBox1.addItem(cuenta);
          }

          comboBox1.addActionListener(e -> {
              String seleccionada = (String) comboBox1.getSelectedItem();
              comboBox2.removeAllItems();
              for (String cuenta : cuentas) {
                  if (!cuenta.equals(seleccionada)) {
                      comboBox2.addItem(cuenta);
                  }
              }
              if (seleccionada != null) {
                  cargarSaldoCuenta(seleccionada);
              }
          });
      }

      private void cargarSaldoCuenta(String numeroCuenta) {
          String query = "SELECT saldo FROM cuenta WHERE numero_cuenta = ?";
      
          try (Connection conn = DBConnection.getConnection();
               PreparedStatement ps = conn.prepareStatement(query)) {
      
              ps.setString(1, numeroCuenta);
              ResultSet rs = ps.executeQuery();
      
              if (rs.next()) {
                  double saldo = rs.getDouble("saldo");
                  saldorigen.setText(String.format("%.2f", saldo));
              } else {
                  saldorigen.setText("0.00");
              }
      
          } catch (SQLException e) {
              JOptionPane.showMessageDialog(null, "Error cargando saldo: " + e.getMessage());
          }
      }
    private void cargarSaldoCuenta(int cuenta_id ) {
        // TODO: conecta a BD y consulta saldo de la cuenta para mostrar

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT saldo FROM Cuenta WHERE cliente_id = ?" )) {
            ps.setInt(1,Integer.parseInt(String.valueOf(cuenta_id)));
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

        try (Connection conn = DBConnection.getConnection();
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

