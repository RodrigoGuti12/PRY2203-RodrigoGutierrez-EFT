package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo de un libro a un estudiante (tabla prestamos).
 *
 * <p>La columna {@code fecha_devolucion} de la base de datos guarda la fecha de
 * VENCIMIENTO: se calcula automáticamente como fecha de préstamo + {@link #PLAZO_DIAS}.
 * El atraso se detecta comparando esa fecha con el día en que se devuelve el libro
 * (o con hoy, si aún no se devuelve).
 *
 * <p>{@code tituloLibro} y {@code nombreEstudiante} no se guardan: se completan con JOIN.
 */
public class Prestamo {

    /** Días de plazo para devolver un libro. */
    public static final int PLAZO_DIAS = 7;

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private boolean devuelto;

    // Datos de solo lectura (JOIN).
    private String tituloLibro;
    private String nombreEstudiante;

    /** Constructor para registros existentes (READ / UPDATE). */
    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                    LocalDate fechaVencimiento, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.devuelto = devuelto;
    }

    /** Constructor para un préstamo nuevo: calcula solo la fecha de vencimiento. */
    public Prestamo(int idEstudiante, int idLibro, LocalDate fechaPrestamo) {
        this(0, idEstudiante, idLibro, fechaPrestamo, fechaPrestamo.plusDays(PLAZO_DIAS), false);
    }

    /** Días de atraso respecto de la fecha indicada (0 si no está vencido). */
    public long diasAtraso(LocalDate referencia) {
        if (referencia.isAfter(fechaVencimiento)) {
            return ChronoUnit.DAYS.between(fechaVencimiento, referencia);
        }
        return 0;
    }

    /** true si no se ha devuelto y ya pasó la fecha de vencimiento. */
    public boolean estaAtrasado() {
        return !devuelto && diasAtraso(LocalDate.now()) > 0;
    }

    /** Texto de estado para mostrar en tablas y reportes. */
    public String getEstado() {
        if (devuelto) {
            return "Devuelto";
        }
        long dias = diasAtraso(LocalDate.now());
        return dias > 0 ? "Atrasado (" + dias + " días)" : "Prestado";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }
}
