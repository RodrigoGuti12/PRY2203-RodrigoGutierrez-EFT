package controlador;

import dao.EstudianteDAO;
import dao.UsuarioDAO;
import modelo.Estudiante;
import modelo.Usuario;
import modelo.UsuarioEstudiante;

import java.sql.SQLException;

/** Controlador de autenticación: valida el ingreso y entrega el usuario con su rol. */
public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    /**
     * Valida correo y contraseña contra la tabla usuarios.
     *
     * @return el usuario (bibliotecario o estudiante) o null si las credenciales son inválidas
     */
    public Usuario login(String correo, String contrasena) throws OperacionException {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isEmpty()) {
            return null;
        }
        try {
            return usuarioDAO.autenticar(correo.trim(), contrasena);
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    /**
     * Para un usuario con rol estudiante, devuelve su ficha de estudiante
     * (se vincula por RUT). Para un bibliotecario devuelve null.
     */
    public Estudiante obtenerEstudiante(Usuario usuario) throws OperacionException {
        if (!(usuario instanceof UsuarioEstudiante)) {
            return null;
        }
        try {
            return estudianteDAO.findByRut(usuario.getRut());
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }
}
