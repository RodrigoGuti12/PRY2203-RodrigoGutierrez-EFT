package controlador;

import dao.PrestamoDAO;
import dao.ReporteDAO;
import modelo.Prestamo;
import modelo.ResultadoReporte;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Controlador de reportes: consulta MySQL y entrega los datos listos para una tabla. */
public class ReporteController {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    /** Reporte 1: libros más prestados. */
    public ResultadoReporte librosMasPrestados() throws OperacionException {
        try {
            return new ResultadoReporte("Libros más prestados",
                    new String[]{"Título", "Autor", "Total de préstamos"},
                    reporteDAO.librosMasPrestados());
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    /** Reporte 2: historial de préstamos de un estudiante. */
    public ResultadoReporte historialPorEstudiante(int idEstudiante) throws OperacionException {
        try {
            List<Object[]> filas = new ArrayList<>();
            for (Prestamo p : prestamoDAO.readPorEstudiante(idEstudiante)) {
                filas.add(new Object[]{p.getId(), p.getTituloLibro(), p.getFechaPrestamo(),
                        p.getFechaVencimiento(), p.getEstado()});
            }
            return new ResultadoReporte("Historial por estudiante",
                    new String[]{"N° préstamo", "Libro", "Fecha préstamo", "Vence", "Estado"}, filas);
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    /** Reporte 3: libros que están actualmente en préstamo (no devueltos). */
    public ResultadoReporte librosEnPrestamo() throws OperacionException {
        try {
            List<Object[]> filas = new ArrayList<>();
            for (Prestamo p : prestamoDAO.readActivos()) {
                filas.add(new Object[]{p.getTituloLibro(), p.getNombreEstudiante(),
                        p.getFechaPrestamo(), p.getFechaVencimiento(), p.getEstado()});
            }
            return new ResultadoReporte("Libros actualmente en préstamo",
                    new String[]{"Libro", "Estudiante", "Fecha préstamo", "Vence", "Estado"}, filas);
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }
}
