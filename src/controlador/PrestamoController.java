package controlador;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.ResultadoOperacion;
import servicio.PrestamoService;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Controlador de préstamos y devoluciones. Las operaciones que modifican datos
 * se ejecutan en HILOS DE SEGUNDO PLANO (ExecutorService) para que la interfaz
 * Swing no se congele; el resultado vuelve a la vista en el hilo de eventos
 * (SwingUtilities.invokeLater). La sincronización del stock está en {@link PrestamoService}.
 */
public class PrestamoController {

    private final PrestamoService servicio = new PrestamoService(); // una única instancia
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    private final AtomicInteger contador = new AtomicInteger(1);
    private final ExecutorService ejecutor = Executors.newFixedThreadPool(8, tarea -> {
        Thread hilo = new Thread(tarea, "prestamo-hilo-" + contador.getAndIncrement());
        hilo.setDaemon(true); // no impide cerrar la aplicación
        return hilo;
    });

    // ---------- Consultas para la vista ----------

    public List<Estudiante> listarEstudiantes() throws OperacionException {
        try {
            return estudianteDAO.readAll();
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    public List<Libro> listarLibros() throws OperacionException {
        try {
            return libroDAO.readAll();
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    /**
     * Lista préstamos.
     *
     * @param soloActivos   true: solo los no devueltos
     * @param idEstudiante  si no es null, solo los de ese estudiante
     */
    public List<Prestamo> listarPrestamos(boolean soloActivos, Integer idEstudiante) throws OperacionException {
        try {
            List<Prestamo> base = (idEstudiante != null)
                    ? prestamoDAO.readPorEstudiante(idEstudiante)
                    : (soloActivos ? prestamoDAO.readActivos() : prestamoDAO.readAll());
            if (idEstudiante != null && soloActivos) {
                base.removeIf(Prestamo::isDevuelto);
            }
            return base;
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    // ---------- Operaciones en segundo plano ----------

    /** Registra un préstamo en un hilo aparte y avisa el resultado en el hilo de Swing. */
    public void prestarAsync(int idLibro, int idEstudiante, Consumer<ResultadoOperacion> alTerminar) {
        ejecutor.submit(() -> {
            ResultadoOperacion resultado = ejecutarPrestamo(idLibro, idEstudiante);
            SwingUtilities.invokeLater(() -> alTerminar.accept(resultado));
        });
    }

    /** Registra una devolución en un hilo aparte y avisa el resultado en el hilo de Swing. */
    public void devolverAsync(int idPrestamo, Consumer<ResultadoOperacion> alTerminar) {
        ejecutor.submit(() -> {
            ResultadoOperacion resultado;
            try {
                resultado = servicio.devolverLibro(idPrestamo);
            } catch (SQLException ex) {
                resultado = ResultadoOperacion.error(ErroresSql.mensaje(ex));
            }
            ResultadoOperacion finalResultado = resultado;
            SwingUtilities.invokeLater(() -> alTerminar.accept(finalResultado));
        });
    }

    /**
     * Prueba de concurrencia: lanza un préstamo del mismo libro por cada estudiante
     * indicado, TODOS AL MISMO TIEMPO, y entrega un resumen. Demuestra que gracias a
     * la sincronización el stock nunca queda negativo ni se presta de más.
     */
    public void simularConcurrente(int idLibro, List<Integer> idsEstudiantes, Consumer<String> alTerminar) {
        Thread coordinador = new Thread(() -> {
            List<Future<ResultadoOperacion>> futuros = new ArrayList<>();
            for (int idEstudiante : idsEstudiantes) {
                futuros.add(ejecutor.submit(() -> ejecutarPrestamo(idLibro, idEstudiante)));
            }
            int exitosos = 0;
            int rechazados = 0;
            for (Future<ResultadoOperacion> f : futuros) {
                try {
                    if (f.get().exito()) {
                        exitosos++;
                    } else {
                        rechazados++;
                    }
                } catch (Exception ex) {
                    rechazados++;
                }
            }
            String stockFinal;
            try {
                Libro libro = libroDAO.findById(idLibro);
                stockFinal = libro == null ? "?" : String.valueOf(libro.getStock());
            } catch (SQLException ex) {
                stockFinal = "?";
            }
            String resumen = "Intentos simultáneos: " + idsEstudiantes.size() + "\n"
                    + "Préstamos registrados: " + exitosos + "\n"
                    + "Rechazados (sin stock): " + rechazados + "\n"
                    + "Stock final del libro: " + stockFinal + " (nunca negativo)";
            SwingUtilities.invokeLater(() -> alTerminar.accept(resumen));
        }, "prestamo-coordinador");
        coordinador.setDaemon(true);
        coordinador.start();
    }

    private ResultadoOperacion ejecutarPrestamo(int idLibro, int idEstudiante) {
        try {
            return servicio.prestarLibro(idLibro, idEstudiante);
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    /** Solo para pruebas: permite acceder al servicio compartido. */
    public PrestamoService getServicio() {
        return servicio;
    }
}
