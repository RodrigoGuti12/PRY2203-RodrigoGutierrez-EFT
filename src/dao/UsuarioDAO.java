package dao;

import config.DatabaseConnection;
import modelo.Rol;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Usuario: todo el SQL de la tabla {@code usuarios} (autenticación y roles).
 * Al leer, crea la subclase correcta de {@link Usuario} según el rol guardado.
 */
public class UsuarioDAO implements CrudDAO<Usuario> {

    /** La columna se llama "contraseña"; se escribe con ñ para no depender de la codificación del archivo. */
    private static final String COL_CLAVE = "`contraseña`";

    private static final String SELECT =
            "SELECT id, nombre, rut, correo, " + COL_CLAVE + " AS clave, rol FROM usuarios";

    @Override
    public boolean create(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, " + COL_CLAVE + ", rol) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol().getValorBd());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Usuario> readAll() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
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
    public Usuario findById(int id) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(SELECT + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Busca un usuario por RUT (para mantener sincronizado un estudiante con su acceso). */
    public Usuario findByRut(String rut) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(SELECT + " WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Autenticación: busca un usuario con ese correo y esa contraseña.
     *
     * @return el usuario (de la subclase que corresponde a su rol) o null si las credenciales no coinciden
     */
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        String sql = SELECT + " WHERE correo = ? AND " + COL_CLAVE + " = ? LIMIT 1";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean update(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, " + COL_CLAVE
                + " = ?, rol = ? WHERE id = ?";
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol().getValorBd());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection cn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return Usuario.crear(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("correo"), rs.getString("clave"), Rol.desdeBd(rs.getString("rol")));
    }
}
