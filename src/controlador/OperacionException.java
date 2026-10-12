package controlador;

/**
 * Error de una operación, con un mensaje ya comprensible para el usuario.
 * Los controladores convierten las SQLException en esta excepción para que
 * la vista no dependa de JDBC.
 */
public class OperacionException extends Exception {

    public OperacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
