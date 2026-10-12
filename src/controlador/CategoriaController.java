package controlador;

import dao.CategoriaDAO;
import modelo.Categoria;
import modelo.ResultadoOperacion;

import java.sql.SQLException;
import java.util.List;

/** Controlador de categorías: valida los datos y delega en {@link CategoriaDAO}. */
public class CategoriaController {

    private final CategoriaDAO dao = new CategoriaDAO();

    public List<Categoria> listar() throws OperacionException {
        try {
            return dao.readAll();
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    public ResultadoOperacion guardar(String nombre) {
        String error = validar(nombre);
        if (error != null) {
            return ResultadoOperacion.error(error);
        }
        try {
            dao.create(new Categoria(nombre.trim()));
            return ResultadoOperacion.ok("Categoría guardada correctamente.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    public ResultadoOperacion actualizar(int id, String nombre) {
        String error = validar(nombre);
        if (error != null) {
            return ResultadoOperacion.error(error);
        }
        try {
            return dao.update(new Categoria(id, nombre.trim()))
                    ? ResultadoOperacion.ok("Categoría actualizada correctamente.")
                    : ResultadoOperacion.error("La categoría ya no existe.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    public ResultadoOperacion eliminar(int id) {
        try {
            return dao.delete(id)
                    ? ResultadoOperacion.ok("Categoría eliminada correctamente.")
                    : ResultadoOperacion.error("La categoría ya no existe.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    private String validar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "El nombre de la categoría no puede estar vacío.";
        }
        if (nombre.trim().length() > 100) {
            return "El nombre no puede superar los 100 caracteres.";
        }
        return null;
    }
}
