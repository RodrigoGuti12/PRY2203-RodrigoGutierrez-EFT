package modelo;

/** Rol de un usuario del sistema (coincide con el ENUM de la tabla usuarios). */
public enum Rol {
    BIBLIOTECARIO("bibliotecario"),
    ESTUDIANTE("estudiante");

    private final String valorBd;

    Rol(String valorBd) {
        this.valorBd = valorBd;
    }

    /** Texto tal como se guarda en MySQL. */
    public String getValorBd() {
        return valorBd;
    }

    /** Convierte el texto de la base de datos en el rol correspondiente. */
    public static Rol desdeBd(String valor) {
        for (Rol r : values()) {
            if (r.valorBd.equalsIgnoreCase(valor)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
