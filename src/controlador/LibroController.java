package controlador;

import dao.CategoriaDAO;
import dao.LibroDAO;
import modelo.Categoria;
import modelo.Libro;
import modelo.ResultadoOperacion;

import java.sql.SQLException;
import java.util.List;

/** Controlador de libros: valida los datos del formulario y delega en {@link LibroDAO}. */
public class LibroController {

    private final LibroDAO dao = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listar() throws OperacionException {
        try {
            return dao.readAll();
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    public List<Libro> buscar(String texto) throws OperacionException {
        try {
            return (texto == null || texto.isBlank()) ? dao.readAll() : dao.buscar(texto.trim());
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    public List<Categoria> listarCategorias() throws OperacionException {
        try {
            return categoriaDAO.readAll();
        } catch (SQLException ex) {
            throw ErroresSql.envolver(ex);
        }
    }

    public ResultadoOperacion guardar(String titulo, String autor, String isbn, String editorial,
                                      String stockTexto, Integer idCategoria) {
        Object r = armar(0, titulo, autor, isbn, editorial, stockTexto, idCategoria);
        if (r instanceof String error) {
            return ResultadoOperacion.error(error);
        }
        try {
            dao.create((Libro) r);
            return ResultadoOperacion.ok("Libro guardado correctamente.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    public ResultadoOperacion actualizar(int id, String titulo, String autor, String isbn,
                                         String editorial, String stockTexto, Integer idCategoria) {
        Object r = armar(id, titulo, autor, isbn, editorial, stockTexto, idCategoria);
        if (r instanceof String error) {
            return ResultadoOperacion.error(error);
        }
        try {
            return dao.update((Libro) r)
                    ? ResultadoOperacion.ok("Libro actualizado correctamente.")
                    : ResultadoOperacion.error("El libro ya no existe.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    public ResultadoOperacion eliminar(int id) {
        try {
            return dao.delete(id)
                    ? ResultadoOperacion.ok("Libro eliminado correctamente.")
                    : ResultadoOperacion.error("El libro ya no existe.");
        } catch (SQLException ex) {
            return ResultadoOperacion.error(ErroresSql.mensaje(ex));
        }
    }

    /** Valida y construye el Libro; si hay un error de validación devuelve el mensaje (String). */
    private Object armar(int id, String titulo, String autor, String isbn, String editorial,
                         String stockTexto, Integer idCategoria) {
        if (vacio(titulo) || titulo.trim().length() > 200) {
            return "El título es obligatorio (máximo 200 caracteres).";
        }
        if (vacio(autor) || autor.trim().length() > 100) {
            return "El autor es obligatorio (máximo 100 caracteres).";
        }
        if (vacio(isbn) || isbn.trim().length() > 20) {
            return "El ISBN es obligatorio (máximo 20 caracteres).";
        }
        if (vacio(editorial) || editorial.trim().length() > 100) {
            return "La editorial es obligatoria (máximo 100 caracteres).";
        }
        int stock;
        try {
            stock = Integer.parseInt(stockTexto == null ? "" : stockTexto.trim());
        } catch (NumberFormatException ex) {
            return "El stock debe ser un número entero.";
        }
        if (stock < 0) {
            return "El stock no puede ser negativo.";
        }
        if (idCategoria == null) {
            return "Seleccione una categoría (si no hay, créela primero en el módulo Categorías).";
        }
        return new Libro(id, titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), stock, idCategoria);
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
