package vista;

import controlador.OperacionException;
import controlador.ReporteController;
import modelo.Estudiante;
import modelo.ResultadoReporte;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Reportes. El bibliotecario ve los tres (libros más prestados, historial por estudiante
 * y libros en préstamo); el estudiante solo ve su propio historial.
 */
public class PanelReportes extends JPanel implements Refrescable {

    private final ReporteController controlador;
    private final Estudiante estudianteFijo; // null = bibliotecario

    private final JComboBox<ComboItem> cmbEstudiante = new JComboBox<>();
    private final JLabel lblTitulo = new JLabel("Seleccione un reporte");
    private DefaultTableModel modelo = Tablas.modelo("");
    private final JTable tabla = Tablas.tabla(modelo, "tablaReporte");

    public PanelReportes(ReporteController controlador, java.util.List<Estudiante> estudiantes,
                         Estudiante estudianteFijo) {
        this.controlador = controlador;
        this.estudianteFijo = estudianteFijo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cmbEstudiante.setName("cmbEstudianteReporte");
        lblTitulo.setName("lblTituloReporte");

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        if (estudianteFijo == null) {
            JButton btnMas = boton("Libros más prestados", "btnReporteMasPrestados");
            JButton btnHist = boton("Historial del estudiante", "btnReporteHistorial");
            JButton btnEnPrestamo = boton("Libros en préstamo", "btnReporteEnPrestamo");
            for (Estudiante e : estudiantes) {
                cmbEstudiante.addItem(new ComboItem(e.getId(), e.getNombre()));
            }
            barra.add(btnMas);
            barra.add(new JLabel("Estudiante:"));
            barra.add(cmbEstudiante);
            barra.add(btnHist);
            barra.add(btnEnPrestamo);
            btnMas.addActionListener(e -> mostrar(() -> controlador.librosMasPrestados()));
            btnHist.addActionListener(e -> {
                ComboItem item = (ComboItem) cmbEstudiante.getSelectedItem();
                if (item == null) {
                    Dialogos.advertencia(this, "Seleccione un estudiante.");
                } else {
                    mostrar(() -> controlador.historialPorEstudiante(item.getId()));
                }
            });
            btnEnPrestamo.addActionListener(e -> mostrar(() -> controlador.librosEnPrestamo()));
        } else {
            JButton btnMio = boton("Actualizar mi historial", "btnMiHistorial");
            barra.add(btnMio);
            btnMio.addActionListener(e -> refrescar());
        }

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(barra, BorderLayout.NORTH);
        norte.add(lblTitulo, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescar();
    }

    private JButton boton(String texto, String nombre) {
        JButton b = new JButton(texto);
        b.setName(nombre);
        return b;
    }

    @Override
    public void refrescar() {
        if (estudianteFijo != null) {
            mostrar(() -> controlador.historialPorEstudiante(estudianteFijo.getId()));
        }
    }

    private interface Consulta {
        ResultadoReporte ejecutar() throws OperacionException;
    }

    private void mostrar(Consulta consulta) {
        try {
            ResultadoReporte r = consulta.ejecutar();
            DefaultTableModel nuevo = Tablas.modelo(r.columnas());
            for (Object[] fila : r.filas()) {
                nuevo.addRow(fila);
            }
            modelo = nuevo;
            tabla.setModel(nuevo);
            lblTitulo.setText(r.titulo() + " (" + r.filas().size() + " registros)");
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }
}
