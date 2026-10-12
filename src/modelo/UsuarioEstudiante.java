package modelo;

import java.util.EnumSet;
import java.util.Set;

/** Usuario con rol estudiante: solo consulta, préstamo y devolución (y su propio historial). */
public class UsuarioEstudiante extends Usuario {

    public UsuarioEstudiante(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo, contrasena);
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }

    @Override
    public Rol getRol() {
        return Rol.ESTUDIANTE;
    }

    @Override
    public Set<Modulo> getModulosPermitidos() {
        return EnumSet.of(Modulo.CONSULTA_LIBROS, Modulo.PRESTAMOS, Modulo.MI_HISTORIAL);
    }
}
