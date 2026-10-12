package dao;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas agregadas para los reportes (datos reales leídos desde MySQL). */
public class ReporteDAO {

    /**
     * Libros más prestados: cuenta los préstamos de cada libro (histórico) y
     * ordena de mayor a menor. Cada fila es {título, autor, total de préstamos}.
     */
    public List<Object[]> librosMasPrestados() throws SQLException {
        String sql = """
                SELECT l.titulo, l.autor, COUNT(p.id) AS total
                FROM libros l
                JOIN prestamos p ON p.id_libro = l.id
                GROUP BY l.id, l.titulo, l.autor
                ORDER BY total DESC, l.titulo
                LIMIT 10
                """;
        List<Object[]> filas = new ArrayList<>();
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[]{rs.getString("titulo"), rs.getString("autor"), rs.getInt("total")});
            }
        }
        return filas;
    }
}
