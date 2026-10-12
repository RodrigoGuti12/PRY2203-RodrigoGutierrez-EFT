package controlador;

import java.sql.SQLException;

/** Traduce errores de MySQL a mensajes claros para el usuario. */
public final class ErroresSql {

    private ErroresSql() {
    }

    /** Devuelve un mensaje comprensible y deja el detalle técnico en la consola (System.err). */
    public static String mensaje(SQLException ex) {
        System.err.println("Error SQL [" + ex.getErrorCode() + "/" + ex.getSQLState() + "]: " + ex.getMessage());

        String texto = ex.getMessage() == null ? "" : ex.getMessage();
        String bajo = texto.toLowerCase();

        if (ex.getErrorCode() == 1451) {
            return "No se puede eliminar: el registro está asociado a otros datos (por ejemplo, préstamos o libros).";
        }
        if (ex.getErrorCode() == 1452) {
            return "Algún dato relacionado (estudiante, libro o categoría) ya no existe.";
        }
        if (ex.getErrorCode() == 1062) {
            return "Ya existe un registro con ese RUT o ISBN.";
        }
        if (ex.getErrorCode() == 1045 || "28000".equals(ex.getSQLState())) {
            return "MySQL rechazó el usuario o la contraseña.\n"
                    + "Revise la variable de entorno MYSQL_PASSWORD (y MYSQL_USER si no usa root).";
        }
        if (ex.getErrorCode() == 1049) {
            return "La base de datos 'biblioteca' no existe.\n"
                    + "Ejecute primero los scripts de la carpeta sql.";
        }
        if (ex.getErrorCode() == 1146) {
            return "Falta alguna tabla en la base de datos.\n"
                    + "Ejecute el script sql/Script_crea_tablas_biblioteca.sql.";
        }
        if (bajo.contains("no suitable driver")) {
            return "No se encontró el driver de MySQL (Connector/J).\n"
                    + "Agréguelo en File > Project Structure > Libraries.";
        }
        if (bajo.contains("communications link failure") || bajo.contains("connection refused")
                || bajo.contains("could not connect")) {
            return "No se pudo conectar con MySQL en localhost:3306.\n"
                    + "Verifique que el servidor MySQL esté iniciado.";
        }
        return texto;
    }

    /** Convierte la SQLException en una OperacionException con mensaje amigable. */
    public static OperacionException envolver(SQLException ex) {
        return new OperacionException(mensaje(ex), ex);
    }
}
