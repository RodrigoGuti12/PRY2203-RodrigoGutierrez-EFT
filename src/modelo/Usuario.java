package modelo;

import java.util.Set;

/**
 * Usuario que ingresa al sistema (tabla usuarios). Clase ABSTRACTA que hereda
 * de {@link Persona}. Sus subclases ({@link UsuarioBibliotecario} y
 * {@link UsuarioEstudiante}) definen el rol y los módulos a los que pueden
 * acceder: la ventana principal solo usa el tipo {@code Usuario}
 * y el comportamiento específico se resuelve por polimorfismo.
 */
public abstract class Usuario extends Persona {

    private String contrasena;

    protected Usuario(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
    }

    /** Rol de este usuario. */
    public abstract Rol getRol();

    /** Módulos del sistema que este tipo de usuario puede abrir. */
    public abstract Set<Modulo> getModulosPermitidos();

    /** Fábrica: crea la subclase que corresponde al rol guardado en la base de datos. */
    public static Usuario crear(int id, String nombre, String rut, String correo,
                                String contrasena, Rol rol) {
        return switch (rol) {
            case BIBLIOTECARIO -> new UsuarioBibliotecario(id, nombre, rut, correo, contrasena);
            case ESTUDIANTE -> new UsuarioEstudiante(id, nombre, rut, correo, contrasena);
        };
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
