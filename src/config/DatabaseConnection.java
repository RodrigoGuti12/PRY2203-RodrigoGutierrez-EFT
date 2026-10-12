package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión a la base de datos MySQL "biblioteca" aplicando el patrón SINGLETON:
 * existe una única instancia de esta clase en toda la aplicación y ella
 * administra una única conexión JDBC, que se reutiliza (persistente) y se
 * vuelve a abrir sola si se cae.
 *
 * <p>Las credenciales NO están escritas en el código: se leen de variables de
 * entorno.
 * <ul>
 *   <li>{@code MYSQL_USER}: usuario (si no existe, se usa "root").</li>
 *   <li>{@code MYSQL_PASSWORD}: contraseña (si no existe, se usa vacía).</li>
 * </ul>
 * En IntelliJ: Run &gt; Edit Configurations &gt; Environment variables.
 *
 * <p>Los DAO piden la conexión con {@code DatabaseConnection.getInstance().getConnection()}
 * y cierran solo sus PreparedStatement/ResultSet, nunca la conexión.
 */
public final class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca"
                    + "?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";

    /** Única instancia (Singleton). */
    private static DatabaseConnection instance;

    /** Única conexión JDBC administrada por el Singleton. */
    private Connection connection;

    /** Constructor privado: nadie más puede crear instancias. */
    private DatabaseConnection() {
    }

    /** Devuelve la única instancia; la crea la primera vez (sincronizado: seguro con hilos). */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Devuelve la conexión compartida. Si todavía no existe, o se cerró o dejó
     * de responder, abre una nueva con DriverManager.getConnection.
     *
     * @throws SQLException si no se puede conectar (clave incorrecta, MySQL apagado, etc.)
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = abrirConexion();
        }
        return connection;
    }

    private Connection abrirConexion() throws SQLException {
        String usuario = System.getenv("MYSQL_USER");
        String clave = System.getenv("MYSQL_PASSWORD");
        if (usuario == null || usuario.isBlank()) {
            usuario = "root";
        }
        if (clave == null) {
            clave = "";
        }
        return DriverManager.getConnection(URL, usuario, clave);
    }

    /** Prueba aislada de conexión (se usa al iniciar la aplicación): abre y ejecuta un SELECT simple. */
    public void probarConexion() throws SQLException {
        try (var st = getConnection().createStatement();
             var rs = st.executeQuery("SELECT 1")) {
            rs.next();
        }
    }

    /** Cierra la conexión (al salir de la aplicación). */
    public synchronized void cerrar() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignorada) {
                // Al cerrar no hay nada más que hacer.
            }
            connection = null;
        }
    }
}
