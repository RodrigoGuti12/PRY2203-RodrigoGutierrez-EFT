package modelo;

/**
 * Libro del inventario (tabla libros). El campo {@code nombreCategoria} no se
 * guarda en la tabla: el DAO lo completa con un JOIN para mostrarlo en pantalla.
 */
public class Libro {

    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private int stock;
    private int idCategoria;
    private String nombreCategoria;

    /** Constructor para registros existentes (READ / UPDATE). */
    public Libro(int id, String titulo, String autor, String isbn, String editorial,
                 int stock, int idCategoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    /** Constructor para registros nuevos (CREATE): el id aún no existe. */
    public Libro(String titulo, String autor, String isbn, String editorial,
                 int stock, int idCategoria) {
        this(0, titulo, autor, isbn, editorial, stock, idCategoria);
    }

    /** true si queda al menos un ejemplar disponible. */
    public boolean hayStock() {
        return stock > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return titulo + " (" + autor + ")";
    }
}
