package modelo;

import java.util.List;

/** Datos de un reporte listo para mostrar en una JTable: columnas y filas. */
public record ResultadoReporte(String titulo, String[] columnas, List<Object[]> filas) {
}
