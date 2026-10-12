package servicio;

import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Libro;
import modelo.Prestamo;
import modelo.ResultadoOperacion;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Lógica de negocio de préstamos y devoluciones.
 *
 * <p>CONCURRENCIA: estos métodos se ejecutan en hilos de segundo plano (los lanza
 * el controlador) y varios pueden llegar al mismo tiempo. Los métodos
 * {@code synchronized} garantizan que solo UN hilo a la vez ejecute la secuencia
 * "comprobar stock → registrar préstamo → descontar stock". Así se evita la
 * condición de carrera en la que dos préstamos aceptan el último ejemplar.
 * (Debe existir una única instancia del servicio; el controlador la crea una vez.)
 *
 * <p>El HISTORIAL del estudiante es la propia tabla {@code prestamos}: cada préstamo
 * registrado queda asociado a su estudiante y se consulta en el reporte de historial.
 */
public class PrestamoService {

    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    /**
     * Pausa (milisegundos) que simula el tiempo de procesamiento de un préstamo.
     * Sirve para evidenciar que la operación corre en segundo plano y que la
     * interfaz NO se congela. Se puede poner en 0 para quitarla.
     */
    private volatile long retardoSimuladoMs = 300;

    public void setRetardoSimuladoMs(long ms) {
        this.retardoSimuladoMs = ms;
    }

    /**
     * Registra un préstamo: verifica disponibilidad, registra el préstamo
     * (con su fecha de vencimiento) y actualiza el stock.
     */
    public synchronized ResultadoOperacion prestarLibro(int idLibro, int idEstudiante) throws SQLException {
        log("inicia préstamo (libro " + idLibro + ", estudiante " + idEstudiante + ")");

        Libro libro = libroDAO.findById(idLibro);
        if (libro == null) {
            return ResultadoOperacion.error("El libro seleccionado ya no existe.");
        }
        if (prestamoDAO.existeActivo(idEstudiante, idLibro)) {
            return ResultadoOperacion.error("El estudiante ya tiene este libro en préstamo sin devolver.");
        }
        if (!libro.hayStock()) {
            return ResultadoOperacion.error("No hay ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }

        simularProcesamiento();

        // El UPDATE solo descuenta si stock > 0: protege el stock incluso a nivel de MySQL.
        if (!libroDAO.descontarStock(idLibro)) {
            return ResultadoOperacion.error("No hay ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }

        Prestamo prestamo = new Prestamo(idEstudiante, idLibro, LocalDate.now());
        try {
            prestamoDAO.create(prestamo); // registra el préstamo y el historial del estudiante
        } catch (SQLException ex) {
            libroDAO.incrementarStock(idLibro); // revierte el descuento si falló el registro
            throw ex;
        }

        Libro actualizado = libroDAO.findById(idLibro);
        log("préstamo registrado (stock restante " + actualizado.getStock() + ")");
        return ResultadoOperacion.ok("Préstamo registrado: \"" + libro.getTitulo() + "\".\n"
                + "Fecha de vencimiento: " + prestamo.getFechaVencimiento()
                + " (" + Prestamo.PLAZO_DIAS + " días).\n"
                + "Stock restante: " + actualizado.getStock());
    }

    /**
     * Registra la devolución de un préstamo, devuelve el ejemplar al stock y
     * detecta si hubo atraso (dejando constancia en el mensaje).
     */
    public synchronized ResultadoOperacion devolverLibro(int idPrestamo) throws SQLException {
        log("inicia devolución (préstamo " + idPrestamo + ")");

        Prestamo prestamo = prestamoDAO.findById(idPrestamo);
        if (prestamo == null) {
            return ResultadoOperacion.error("El préstamo seleccionado no existe.");
        }
        if (prestamo.isDevuelto()) {
            return ResultadoOperacion.error("Este préstamo ya fue devuelto.");
        }

        simularProcesamiento();

        if (!prestamoDAO.marcarDevuelto(idPrestamo)) {
            return ResultadoOperacion.error("Este préstamo ya fue devuelto.");
        }
        libroDAO.incrementarStock(prestamo.getIdLibro());

        long atraso = prestamo.diasAtraso(LocalDate.now());
        log("devolución registrada (atraso " + atraso + " días)");
        if (atraso > 0) {
            return ResultadoOperacion.ok("Devolución registrada: \"" + prestamo.getTituloLibro() + "\".\n"
                    + "ATRASO: " + atraso + " día(s). Vencía el " + prestamo.getFechaVencimiento()
                    + " y se devolvió el " + LocalDate.now() + ".");
        }
        return ResultadoOperacion.ok("Devolución registrada: \"" + prestamo.getTituloLibro()
                + "\". Entregado a tiempo.");
    }

    private void simularProcesamiento() {
        if (retardoSimuladoMs > 0) {
            try {
                Thread.sleep(retardoSimuladoMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void log(String mensaje) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + mensaje);
    }
}
