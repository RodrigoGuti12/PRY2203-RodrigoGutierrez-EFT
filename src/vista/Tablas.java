package vista;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Utilidades para crear tablas de solo lectura (las filas se editan desde el formulario). */
final class Tablas {

    private Tablas() {
    }

    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            /** Devuelve el tipo real de la columna para que los números se ordenen como números (1, 2, 10). */
            @Override
            public Class<?> getColumnClass(int columna) {
                for (int fila = 0; fila < getRowCount(); fila++) {
                    Object valor = getValueAt(fila, columna);
                    if (valor != null) {
                        return valor.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    static JTable tabla(DefaultTableModel modelo, String nombre) {
        JTable t = new JTable(modelo);
        t.setName(nombre);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(22);
        t.setAutoCreateRowSorter(true);
        // Los números se alinean a la izquierda, igual que sus encabezados, para no confundirlos con la columna vecina.
        DefaultTableCellRenderer izquierda = new DefaultTableCellRenderer();
        izquierda.setHorizontalAlignment(SwingConstants.LEFT);
        t.setDefaultRenderer(Number.class, izquierda);
        return t;
    }
}