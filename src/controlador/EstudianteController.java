package controlador;

import dao.EstudianteDAO;
import dao.UsuarioDAO;
import modelo.Estudiante;
import modelo.ResultadoOperacion;
import modelo.Rol;
import modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de estudiantes. Registrar un estudiante también crea su acceso
 * al sistema (tabla usuarios, rol estudiante), vinculado por RUT, para que
 * pueda ingresar y hacer sus préstamos.
 */
public class EstudianteController {

    private final EstudianteDAO dao = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() throws OperacionException {
        try {
            return dao.readAll();
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    /** Registra el estudiante y su usuario de acceso (la contraseña es obligatoria al crear). */
    public ResultadoOperacion guardar(String nombre, String rut, String curso, String correo, String clave) {
        String error = validar(nombre, rut, curso, correo);
        if (error == null && (clave == null || clave.length() < 4)) {
            error = "La contraseña de acceso es obligatoria (mínimo 4 caracteres).";
        }
        if (error != null) {
            return ResultadoOperacion.error(error);
        }
        try {
            dao.create(new Estudiante(nombre.trim(), rut.trim(), curso.trim(), correo.trim()));
            try {
                usuarioDAO.create(Usuario.crear(0, nombre.trim(), rut.trim(), correo.trim(), clave, Rol.ESTUDIANTE));
            } catch (SQLException exUsuario) {
                // Si no se pudo crear el acceso, se deshace el registro del estudiante.
                Estudiante creado = dao.findByRut(rut.trim());
                if (creado != null) {
                    dao.delete(creado.getId());
                }
                throw exUsuario;
            }
            return ResultadoOperacion.ok("Estudiante registrado correctamente (con acceso al sistema).");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    /** Actualiza el estudiante y su usuario. Si la contraseña viene vacía, no se cambia. */
    public ResultadoOperacion actualizar(int id, String nombre, String rut, String curso,
                                         String correo, String clave) {
        String error = validar(nombre, rut, curso, correo);
        if (error == null && clave != null && !clave.isEmpty() && clave.length() < 4) {
            error = "La contraseña debe tener al menos 4 caracteres (o déjela vacía para no cambiarla).";
        }
        if (error != null) {
            return ResultadoOperacion.error(error);
        }
        try {
            Estudiante anterior = dao.findById(id);
            if (anterior == null) {
                return ResultadoOperacion.error("El estudiante ya no existe.");
            }
            dao.update(new Estudiante(id, nombre.trim(), rut.trim(), curso.trim(), correo.trim()));

            // Mantiene sincronizado el usuario de acceso (se ubica por el RUT anterior).
            Usuario usuario = usuarioDAO.findByRut(anterior.getRut());
            if (usuario != null) {
                usuario.setNombre(nombre.trim());
                usuario.setRut(rut.trim());
                usuario.setCorreo(correo.trim());
                if (clave != null && !clave.isEmpty()) {
                    usuario.setContrasena(clave);
                }
                usuarioDAO.update(usuario);
            } else if (clave != null && !clave.isEmpty()) {
                usuarioDAO.create(Usuario.crear(0, nombre.trim(), rut.trim(), correo.trim(), clave, Rol.ESTUDIANTE));
            }
            return ResultadoOperacion.ok("Estudiante actualizado correctamente.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    /** Elimina el estudiante (si no tiene préstamos) y su usuario de acceso. */
    public ResultadoOperacion eliminar(int id) {
        try {
            Estudiante e = dao.findById(id);
            if (e == null) {
                return ResultadoOperacion.error("El estudiante ya no existe.");
            }
            dao.delete(id); // si tiene préstamos, MySQL lo impide (clave foránea)
            Usuario u = usuarioDAO.findByRut(e.getRut());
            if (u != null) {
                usuarioDAO.delete(u.getId());
            }
            return ResultadoOperacion.ok("Estudiante eliminado correctamente.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    private String validar(String nombre, String rut, String curso, String correo) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 100) {
            return "El nombre es obligatorio (máximo 100 caracteres).";
        }
        if (rut == null || !rut.trim().matches("\\d{1,8}-[0-9kK]")) {
            return "El RUT debe tener el formato 12345678-9 (sin puntos y con guion).";
        }
        if (curso == null || curso.isBlank() || curso.trim().length() > 20) {
            return "El curso es obligatorio (máximo 20 caracteres).";
        }
        if (correo == null || !correo.trim().matches("[^@\\s]+@[^@\\s]+") || correo.trim().length() > 100) {
            return "Ingrese un correo válido (ejemplo: nombre@correo.cl).";
        }
        return null;
    }
}
