package app;

import config.DatabaseConnection;
import vista.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/** Punto de entrada: verifica la conexión a MySQL y abre el inicio de sesión. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // se usa el aspecto por defecto
        }
        try {
            DatabaseConnection.getInstance().probarConexion();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos 'biblioteca'.\n"
                            + "Revise que MySQL esté activo, que haya ejecutado los dos scripts SQL\n"
                            + "y que la variable MYSQL_PASSWORD esté definida.\n\nDetalle: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DatabaseConnection.getInstance().cerrar()));
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
