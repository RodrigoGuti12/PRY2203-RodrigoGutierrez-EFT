package modelo;

import java.util.EnumSet;
import java.util.Set;

/** Usuario con rol bibliotecario: gestión total del sistema. */
public class UsuarioBibliotecario extends Usuario {

    public UsuarioBibliotecario(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo, contrasena);
    }

    @Override
    public String getTipo() {
        return "Bibliotecario";
    }

    @Override
    public Rol getRol() {
        return Rol.BIBLIOTECARIO;
    }

    @Override
    public Set<Modulo> getModulosPermitidos() {
        return EnumSet.of(Modulo.LIBROS, Modulo.CATEGORIAS, Modulo.ESTUDIANTES,
                Modulo.PRESTAMOS, Modulo.REPORTES);
    }
}
