package vista;

import controlador.AuthController;
import controlador.OperacionException;
import modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Ventana de inicio de sesión (correo y contraseña de la tabla usuarios). */
public class LoginFrame extends JFrame {

    private final AuthController auth = new AuthController();
    private final JTextField txtCorreo = new JTextField(22);
    private final JPasswordField txtClave = new JPasswordField(22);
    private final JButton btnIngresar = new JButton("Ingresar");

    public LoginFrame() {
        super("Biblioteca Escolar - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        txtCorreo.setName("txtCorreoLogin");
        txtClave.setName("txtClaveLogin");
        btnIngresar.setName("btnIngresar");

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel titulo = new JLabel("Sistema de Gestión de Biblioteca Escolar", JLabel.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        p.add(titulo, c);
        c.gridwidth = 1;
        c.gridy = 1;
        p.add(new JLabel("Correo:"), c);
        c.gridx = 1;
        p.add(txtCorreo, c);
        c.gridx = 0;
        c.gridy = 2;
        p.add(new JLabel("Contraseña:"), c);
        c.gridx = 1;
        p.add(txtClave, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        p.add(btnIngresar, c);
        setContentPane(p);

        btnIngresar.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());
        txtCorreo.addActionListener(e -> txtClave.requestFocusInWindow());
        getRootPane().setDefaultButton(btnIngresar);

        pack();
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        String correo = txtCorreo.getText().trim();
        String clave = new String(txtClave.getPassword());
        if (correo.isEmpty() || clave.isEmpty()) {
            Dialogos.advertencia(this, "Ingrese su correo y su contraseña.");
            return;
        }
        try {
            Usuario usuario = auth.login(correo, clave);
            if (usuario == null) {
                Dialogos.advertencia(this, "Correo o contraseña incorrectos.");
                txtClave.setText("");
                return;
            }
            MainFrame principal = new MainFrame(usuario, auth.obtenerEstudiante(usuario));
            principal.setVisible(true);
            dispose();
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }
}
