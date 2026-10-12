package dao;

import config.DatabaseConnection;
import modelo.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Prestamo: todo el SQL de la tabla {@code prestamos}. Las lecturas hacen
 * JOIN con estudiantes y libros para mostrar nombres y títulos legibles.
 */
public class PrestamoDAO implements CrudDAO<Prestamo> {

    private static final String SELECT_BASE = """
            SELECT p.id, p.id_estudiante, e.nombre AS estudiante, p.id_libro, l.titulo,
                   p.fecha_prestamo, p.fecha_devolucion, p.devuelto
            FROM prestamos p
            JOIN estudiantes e ON p.id_estudiante = e.id
            JOIN libros l ON p.id_libro = l.id
            """;

    @Override
    public boolean create(Prestamo p) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, "
                + "fecha_devolucion, devuelto) VALUES (?, ?, ?, ?, ?)";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaVencimiento()));
            ps.setBoolean(5, p.isDevuelto());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Prestamo> readAll() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY p.id", null);
    }

    /** Préstamos que aún no se han devuelto. */
    public List<Prestamo> readActivos() throws SQLException {
        return consultar(SELECT_BASE + " WHERE p.devuelto = FALSE ORDER BY p.id", null);
    }

    /** Historial completo de un estudiante (devueltos y no devueltos). */
    public List<Prestamo> readPorEstudiante(int idEstudiante) throws SQLException {
        return consultar(SELECT_BASE + " WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC",
                idEstudiante);
    }

    private List<Prestamo> consultar(String sql, Integer parametro) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public Prestamo findById(int id) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** true si el estudiante ya tiene ese libro en préstamo sin devolver. */
    public boolean existeActivo(int idEstudiante, int idLibro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? "
                + "AND devuelto = FALSE";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean update(Prestamo p) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaVencimiento()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Marca un préstamo como devuelto SOLO si aún no lo estaba
     * (evita devolver dos veces el mismo préstamo).
     */
    public boolean marcarDevuelto(int idPrestamo) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE")) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo(rs.getInt("id"), rs.getInt("id_estudiante"), rs.getInt("id_libro"),
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto"));
        p.setNombreEstudiante(rs.getString("estudiante"));
        p.setTituloLibro(rs.getString("titulo"));
        return p;
    }
}
