package vista;

/**
 * Elemento de un JComboBox que muestra un texto legible ("3 - Dune") pero
 * conserva internamente el id, que es lo que se guarda en la base de datos.
 */
public class ComboItem {

    private final int id;
    private final String texto;

    public ComboItem(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public int getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return id + " - " + texto;
    }
}
