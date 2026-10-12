package modelo;

/** Módulos (pantallas) del sistema. Cada rol tiene acceso a algunos de ellos. */
public enum Modulo {
    LIBROS("Gestión de libros"),
    CATEGORIAS("Categorías"),
    ESTUDIANTES("Gestión de estudiantes"),
    PRESTAMOS("Préstamos y devoluciones"),
    REPORTES("Reportes"),
    CONSULTA_LIBROS("Consulta de libros"),
    MI_HISTORIAL("Mi historial");

    private final String titulo;

    Modulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
