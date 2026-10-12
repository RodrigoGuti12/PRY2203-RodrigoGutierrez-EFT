package dao;

import config.DatabaseConnection;
import modelo.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO de Libro: todo el SQL de la tabla {@code libros} (con el nombre de la categoría vía JOIN). */
public class LibroDAO implements CrudDAO<Libro> {

    private static final String SELECT_BASE = """
            SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock,
                   l.id_categoria, c.nombre AS categoria
            FROM libros l
            LEFT JOIN categorias c ON l.id_categoria = c.id
            """;

    @Override
    public boolean create(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Libro> readAll() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY l.id", null);
    }

    /** Busca libros cuyo título, autor o ISBN contengan el texto indicado. */
    public List<Libro> buscar(String texto) throws SQLException {
        return consultar(SELECT_BASE
                + " WHERE l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? ORDER BY l.id",
                "%" + texto + "%");
    }

    private List<Libro> consultar(String sql, String filtro) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (filtro != null) {
                ps.setString(1, filtro);
                ps.setString(2, filtro);
                ps.setString(3, filtro);
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
    public Libro findById(int id) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE l.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean update(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, "
                + "stock = ?, id_categoria = ? WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.setInt(7, l.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM libros WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Descuenta un ejemplar SOLO si queda stock. La condición {@code stock > 0}
     * dentro del UPDATE hace que el propio MySQL impida dejar el stock en negativo,
     * incluso si dos operaciones llegaran a la vez (segunda línea de defensa
     * además de {@code synchronized}).
     *
     * @return true si se descontó un ejemplar; false si no quedaba stock
     */
    public boolean descontarStock(int idLibro) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    /** Devuelve un ejemplar al stock (devolución de un préstamo o reversa de un error). */
    public boolean incrementarStock(int idLibro) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE libros SET stock = stock + 1 WHERE id = ?")) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("isbn"), rs.getString("editorial"), rs.getInt("stock"),
                rs.getInt("id_categoria"));
        l.setNombreCategoria(rs.getString("categoria"));
        return l;
    }
}
