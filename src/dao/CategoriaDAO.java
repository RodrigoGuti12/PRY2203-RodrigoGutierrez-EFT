package dao;

import config.DatabaseConnection;
import modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO de Categoria: todo el SQL de la tabla {@code categorias}. */
public class CategoriaDAO implements CrudDAO<Categoria> {

    @Override
    public boolean create(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias (nombre) VALUES (?)";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Categoria> readAll() throws SQLException {
        String sql = "SELECT id, nombre FROM categorias ORDER BY id";
        List<Categoria> lista = new ArrayList<>();
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    @Override
    public Categoria findById(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Categoria(rs.getInt("id"), rs.getString("nombre")) : null;
            }
        }
    }

    @Override
    public boolean update(Categoria categoria) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
