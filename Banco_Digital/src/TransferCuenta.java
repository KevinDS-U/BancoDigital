import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**public class TransferCuenta {
    private JPanel TRANFER;
    private JComboBox TipoTrans;
    private JPanel p1;
    private JLabel tex1;
    private JLabel tex2;
    private JComboBox comboBox1;
    private JLabel origen;
    private JComboBox comboBox2;
    private JLabel Destino;
    private JTextField monto;
    private JLabel labelmonto;
    private JTextField saldorigen;
    private JLabel NumCuentads;
    private JTextField numcd;
    private JButton enviarBoton;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }*/
    /**
     * Pantalla de transferencias bancarias.
     * Permite transferencias internas (entre cuentas propias) y externas (a terceros).
     * Usa JComboBox y JTextField según el tipo de transferencia seleccionado.
     */
    public class TransferCuenta {
        // Panel principal del formulario
        private JPanel TRANFER;

        // ComboBox para seleccionar tipo de transferencia
        private JComboBox<String> TipoTrans;

        // Panel contenedor
        private JPanel p1;

        // Etiquetas descriptivas
        private JLabel tex1;
        private JLabel tex2;

        // Cuenta origen
        private JLabel origen;
        private JComboBox<String> comboBox1; // Lista de cuentas del cliente

        // Cuenta destino
        private JLabel Destino;
        private JComboBox<String> comboBox2; // Solo para transferencia interna

        // Campo para número de cuenta destino en transferencia externa
        private JLabel NumCuentads;
        private JTextField numcd;

        // Saldo de la cuenta origen
        private JTextField saldorigen;

        // Monto a transferir
        private JLabel labelmonto;
        private JTextField monto;

        // Botón para enviar
        private JButton enviarBoton;

        // Constructor
        public TransferCuenta() {
            // Inicializar lógica cuando cambia el tipo de transferencia
            TipoTrans.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actualizarTipoTransferencia();
                }
            });

            // Acción del botón de envío
            enviarBoton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ejecutarTransferencia();
                }
            });

            // Inicialización visual
            actualizarTipoTransferencia(); // Oculta campos innecesarios al inicio
        }

        /**
         * Muestra u oculta campos según el tipo de transferencia seleccionado.
         */
        private void actualizarTipoTransferencia() {
            String tipo = (String) TipoTrans.getSelectedItem();

            if ("Entre mis cuentas".equals(tipo)) {
                comboBox2.setVisible(true);
                Destino.setVisible(true);

                numcd.setVisible(false);
                NumCuentads.setVisible(false);
            } else if ("A terceros".equals(tipo)) {
                comboBox2.setVisible(false);
                Destino.setVisible(false);

                numcd.setVisible(true);
                NumCuentads.setVisible(true);
            } else {
                comboBox2.setVisible(false);
                Destino.setVisible(false);
                numcd.setVisible(false);
                NumCuentads.setVisible(false);
            }
        }

        /**
         * Valida los datos ingresados y simula una transferencia.
         */
        private void ejecutarTransferencia() {
            String tipo = (String) TipoTrans.getSelectedItem();
            String cuentaOrigen = (String) comboBox1.getSelectedItem();
            String cuentaDestino = tipo.equals("Entre mis cuentas") ?
                    (String) comboBox2.getSelectedItem() : numcd.getText().trim();
            String montoTexto = monto.getText().trim();

            // Validaciones
            if (cuentaOrigen == null || cuentaOrigen.isEmpty()) {
                mostrarError("Seleccione una cuenta de origen.");
                return;
            }

            if (cuentaDestino == null || cuentaDestino.isEmpty()) {
                mostrarError("Indique la cuenta de destino.");
                return;
            }

            if (montoTexto.isEmpty()) {
                mostrarError("Ingrese un monto.");
                return;
            }

            double valor;
            try {
                valor = Double.parseDouble(montoTexto);
                if (valor <= 0) {
                    mostrarError("El monto debe ser mayor que cero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarError("El monto debe ser un número válido.");
                return;
            }

            // Aquí llamarías a tu procedimiento almacenado según tipo
            if (tipo.equals("Entre mis cuentas")) {
                // Lógica: llamar a sp_TransferenciaInterna
                JOptionPane.showMessageDialog(null,
                        "Transferencia interna realizada:\nDe: " + cuentaOrigen + "\nA: " + cuentaDestino + "\nMonto: $" + valor);
            } else {
                // Lógica: llamar a sp_TransferenciaExterna
                JOptionPane.showMessageDialog(null,
                        "Transferencia a terceros realizada:\nDe: " + cuentaOrigen + "\nA: " + cuentaDestino + "\nMonto: $" + valor);
            }
        }

        /**
         * Muestra un mensaje de error al usuario.
         */
        private void mostrarError(String mensaje) {
            JOptionPane.showMessageDialog(null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        }

        /**
         * Método requerido por el Form Designer si defines componentes personalizados.
         */
        private void createUIComponents() {
            // Puedes dejar esto vacío si no hay componentes personalizados.
        }
    }


