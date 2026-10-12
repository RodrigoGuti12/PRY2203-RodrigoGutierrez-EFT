package modelo;

/** Estudiante de la biblioteca (tabla estudiantes). Hereda de {@link Persona}. */
public class Estudiante extends Persona {

    private String curso;

    /** Constructor para registros existentes (READ / UPDATE). */
    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    /** Constructor para registros nuevos (CREATE): el id aún no existe. */
    public Estudiante(String nombre, String rut, String curso, String correo) {
        this(0, nombre, rut, curso, correo);
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }
}
