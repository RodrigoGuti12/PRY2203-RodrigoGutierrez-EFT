package dao;

import config.DatabaseConnection;
import modelo.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO de Estudiante: todo el SQL de la tabla {@code estudiantes}. */
public class EstudianteDAO implements CrudDAO<Estudiante> {

    private static final String SELECT = "SELECT id, nombre, rut, curso, correo FROM estudiantes";

    @Override
    public boolean create(Estudiante e) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Estudiante> readAll() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(SELECT + " ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Estudiante findById(int id) throws SQLException {
        return buscarUno(SELECT + " WHERE id = ?", String.valueOf(id), true);
    }

    /** Busca un estudiante por su RUT (vincula un usuario con su ficha de estudiante). */
    public Estudiante findByRut(String rut) throws SQLException {
        return buscarUno(SELECT + " WHERE rut = ?", rut, false);
    }

    private Estudiante buscarUno(String sql, String valor, boolean numerico) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (numerico) {
                ps.setInt(1, Integer.parseInt(valor));
            } else {
                ps.setString(1, valor);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean update(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }
}
