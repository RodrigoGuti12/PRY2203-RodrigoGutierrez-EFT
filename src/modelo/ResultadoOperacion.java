package modelo;

/**
 * Resultado de una operación de negocio: si funcionó o no y el mensaje que se
 * muestra al usuario. Permite que controladores y servicio informen a la vista
 * sin que esta conozca detalles de SQL.
 */
public record ResultadoOperacion(boolean exito, String mensaje) {

    public static ResultadoOperacion ok(String mensaje) {
        return new ResultadoOperacion(true, mensaje);
    }

    public static ResultadoOperacion error(String mensaje) {
        return new ResultadoOperacion(false, mensaje);
    }
}
