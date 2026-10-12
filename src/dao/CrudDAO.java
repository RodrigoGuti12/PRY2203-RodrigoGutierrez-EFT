package dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato (INTERFAZ) que cumplen todos los DAO del sistema: las cuatro
 * operaciones CRUD sobre una entidad {@code T}. Cada DAO se encarga
 * exclusivamente de hablar con la base de datos.
 *
 * @param <T> entidad que maneja el DAO
 */
public interface CrudDAO<T> {

    /** CREATE: inserta la entidad. @return true si se insertó exactamente una fila. */
    boolean create(T entidad) throws SQLException;

    /** READ: devuelve todas las filas como objetos. */
    List<T> readAll() throws SQLException;

    /** READ: busca por id; null si no existe. */
    T findById(int id) throws SQLException;

    /** UPDATE: modifica la fila con el id de la entidad. @return true si se actualizó una fila. */
    boolean update(T entidad) throws SQLException;

    /** DELETE: elimina la fila con ese id. @return true si se eliminó una fila. */
    boolean delete(int id) throws SQLException;
}
