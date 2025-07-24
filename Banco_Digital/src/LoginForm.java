import javax.swing.*;

public class LoginForm extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginForm() {
        setTitle("Login - Banco Dinamo 24/7");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setBounds(30, 30, 80, 25);
        add(userLabel);

        userField = new JTextField();
        userField.setBounds(120, 30, 160, 25);
        add(userField);

        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setBounds(30, 70, 80, 25);
        add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(120, 70, 160, 25);
        add(passField);

        loginButton = new JButton("Iniciar sesión");
        loginButton.setBounds(90, 110, 150, 25);
        add(loginButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            AuthService.LoginResult result = AuthService.login(username, password);

            switch (result) {
                case SUCCESS -> JOptionPane.showMessageDialog(this, "Login exitoso.");
                case WRONG_CREDENTIALS -> JOptionPane.showMessageDialog(this, "Credenciales incorrectas.");
                case BLOCKED -> JOptionPane.showMessageDialog(this, "Cuenta bloqueada por intentos fallidos.");
            }
        });
    }
}