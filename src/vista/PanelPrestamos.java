package vista;

import controlador.OperacionException;
import controlador.PrestamoController;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.ResultadoOperacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de préstamos y devoluciones. El bibliotecario gestiona todos los préstamos;
 * el estudiante solo puede pedir libros a su nombre y ver los suyos.
 * Las operaciones corren en hilos de segundo plano: mientras se procesan,
 * los botones se desactivan y la ventana sigue respondiendo.
 */
public class PanelPrestamos extends JPanel implements Refrescable {

    private final PrestamoController controlador;
    private final Estudiante estudianteFijo; // null = bibliotecario (elige cualquier estudiante)

    private final JComboBox<ComboItem> cmbEstudiante = new JComboBox<>();
    private final JComboBox<ComboItem> cmbLibro = new JComboBox<>();
    private final JCheckBox chkActivos = new JCheckBox("Mostrar solo préstamos activos");
    private final JLabel lblEstado = new JLabel(" ");
    private final JButton btnPrestar = new JButton("Prestar libro");
    private final JButton btnDevolver = new JButton("Registrar devolución");
    private final JButton btnSimular = new JButton("Simular préstamos simultáneos");

    private final DefaultTableModel modelo = Tablas.modelo(
            "ID", "Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado");
    private final JTable tabla = Tablas.tabla(modelo, "tablaPrestamos");
    private List<Prestamo> cache = new ArrayList<>();

    public PanelPrestamos(PrestamoController controlador, Estudiante estudianteFijo) {
        this.controlador = controlador;
        this.estudianteFijo = estudianteFijo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cmbEstudiante.setName("cmbEstudiantePrestamo");
        cmbLibro.setName("cmbLibroPrestamo");
        chkActivos.setName("chkActivos");
        lblEstado.setName("lblEstadoPrestamo");
        btnPrestar.setName("btnPrestar");
        btnDevolver.setName("btnDevolver");
        btnSimular.setName("btnSimular");
        cmbLibro.setPrototypeDisplayValue(new ComboItem(999, "Título de libro largo de ejemplo (stock 99)"));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.add(new JLabel("Estudiante:"));
        fila1.add(cmbEstudiante);
        fila1.add(new JLabel("Libro:"));
        fila1.add(cmbLibro);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.add(btnPrestar);
        if (estudianteFijo == null) {
            fila2.add(btnDevolver);
            fila2.add(btnSimular);
        }
        fila2.add(chkActivos);

        JPanel norte = new JPanel(new BorderLayout());
        JPanel formulario = new JPanel();
        formulario.setLayout(new javax.swing.BoxLayout(formulario, javax.swing.BoxLayout.Y_AXIS));
        formulario.add(fila1);
        formulario.add(fila2);
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(lblEstado, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnPrestar.addActionListener(e -> prestar());
        btnDevolver.addActionListener(e -> devolver());
        btnSimular.addActionListener(e -> simular());
        chkActivos.addActionListener(e -> cargarTabla());

        refrescar();
    }

    @Override
    public void refrescar() {
        try {
            cmbEstudiante.removeAllItems();
            if (estudianteFijo != null) {
                cmbEstudiante.addItem(new ComboItem(estudianteFijo.getId(), estudianteFijo.getNombre()));
                cmbEstudiante.setEnabled(false);
            } else {
                for (Estudiante e : controlador.listarEstudiantes()) {
                    cmbEstudiante.addItem(new ComboItem(e.getId(), e.getNombre()));
                }
            }
            ComboItem anterior = (ComboItem) cmbLibro.getSelectedItem();
            cmbLibro.removeAllItems();
            for (Libro l : controlador.listarLibros()) {
                cmbLibro.addItem(new ComboItem(l.getId(), l.getTitulo() + " (stock " + l.getStock() + ")"));
            }
            if (anterior != null) {
                for (int i = 0; i < cmbLibro.getItemCount(); i++) {
                    if (cmbLibro.getItemAt(i).getId() == anterior.getId()) {
                        cmbLibro.setSelectedIndex(i);
                        break;
                    }
                }
            }
            cargarTabla();
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void cargarTabla() {
        try {
            Integer idEst = estudianteFijo == null ? null : estudianteFijo.getId();
            cache = controlador.listarPrestamos(chkActivos.isSelected(), idEst);
            modelo.setRowCount(0);
            for (Prestamo p : cache) {
                modelo.addRow(new Object[]{p.getId(), p.getNombreEstudiante(), p.getTituloLibro(),
                        p.getFechaPrestamo(), p.getFechaVencimiento(), p.getEstado()});
            }
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void ocupado(boolean ocupado, String mensaje) {
        btnPrestar.setEnabled(!ocupado);
        btnDevolver.setEnabled(!ocupado);
        btnSimular.setEnabled(!ocupado);
        lblEstado.setText(mensaje);
    }

    private void prestar() {
        ComboItem est = (ComboItem) cmbEstudiante.getSelectedItem();
        ComboItem lib = (ComboItem) cmbLibro.getSelectedItem();
        if (est == null || lib == null) {
            Dialogos.advertencia(this, "Seleccione un estudiante y un libro.");
            return;
        }
        ocupado(true, "Procesando préstamo en segundo plano...");
        controlador.prestarAsync(lib.getId(), est.getId(), this::terminado);
    }

    private void devolver() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Dialogos.advertencia(this, "Seleccione en la tabla el préstamo a devolver.");
            return;
        }
        int id = (Integer) tabla.getValueAt(fila, 0);
        ocupado(true, "Procesando devolución en segundo plano...");
        controlador.devolverAsync(id, this::terminado);
    }

    private void terminado(ResultadoOperacion r) {
        ocupado(false, " ");
        if (r.exito()) {
            Dialogos.info(this, r.mensaje());
        } else {
            Dialogos.advertencia(this, r.mensaje());
        }
        refrescar();
    }

    private void simular() {
        ComboItem lib = (ComboItem) cmbLibro.getSelectedItem();
        if (lib == null) {
            Dialogos.advertencia(this, "Seleccione el libro para la simulación.");
            return;
        }
        if (!Dialogos.confirmar(this, "Se intentará prestar el libro \"" + lib.getTexto()
                + "\" a todos los estudiantes AL MISMO TIEMPO.\nEsto registra préstamos reales en la base de datos. ¿Continuar?")) {
            return;
        }
        try {
            List<Integer> ids = new ArrayList<>();
            for (Estudiante e : controlador.listarEstudiantes()) {
                ids.add(e.getId());
            }
            ocupado(true, "Simulando " + ids.size() + " préstamos simultáneos...");
            controlador.simularConcurrente(lib.getId(), ids, resumen -> {
                ocupado(false, " ");
                Dialogos.info(this, resumen);
                refrescar();
            });
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }
}
