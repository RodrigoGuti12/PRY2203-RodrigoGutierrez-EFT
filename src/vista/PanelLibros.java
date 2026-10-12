package vista;

import controlador.LibroController;
import controlador.OperacionException;
import modelo.Categoria;
import modelo.Libro;
import modelo.ResultadoOperacion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de libros. Con {@code soloLectura = false} (bibliotecario) es un CRUD
 * completo; con {@code true} (estudiante) solo permite buscar y consultar.
 */
public class PanelLibros extends JPanel implements Refrescable {

    private final LibroController controlador;

    private final JTextField txtTitulo = new JTextField(20);
    private final JTextField txtAutor = new JTextField(16);
    private final JTextField txtIsbn = new JTextField(13);
    private final JTextField txtEditorial = new JTextField(14);
    private final JTextField txtStock = new JTextField(4);
    private final JComboBox<ComboItem> cmbCategoria = new JComboBox<>();
    private final JTextField txtBuscar = new JTextField(18);

    private final DefaultTableModel modelo = Tablas.modelo(
            "ID", "Título", "Autor", "ISBN", "Editorial", "Stock", "Categoría");
    private final JTable tabla = Tablas.tabla(modelo, "tablaLibros");
    private List<Libro> cache = new ArrayList<>();

    public PanelLibros(LibroController controlador, boolean soloLectura) {
        this.controlador = controlador;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtTitulo.setName("txtTituloLibro");
        txtAutor.setName("txtAutorLibro");
        txtIsbn.setName("txtIsbnLibro");
        txtEditorial.setName("txtEditorialLibro");
        txtStock.setName("txtStockLibro");
        cmbCategoria.setName("cmbCategoriaLibro");
        txtBuscar.setName("txtBuscarLibro");

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));

        if (!soloLectura) {
            JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            fila1.add(new JLabel("Título:"));
            fila1.add(txtTitulo);
            fila1.add(new JLabel("Autor:"));
            fila1.add(txtAutor);
            fila1.add(new JLabel("ISBN:"));
            fila1.add(txtIsbn);

            JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            fila2.add(new JLabel("Editorial:"));
            fila2.add(txtEditorial);
            fila2.add(new JLabel("Stock:"));
            fila2.add(txtStock);
            fila2.add(new JLabel("Categoría:"));
            fila2.add(cmbCategoria);

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            JButton btnGuardar = boton("Guardar", "btnGuardarLibro");
            JButton btnActualizar = boton("Actualizar", "btnActualizarLibro");
            JButton btnEliminar = boton("Eliminar", "btnEliminarLibro");
            JButton btnLimpiar = boton("Limpiar", "btnLimpiarLibro");
            botones.add(btnGuardar);
            botones.add(btnActualizar);
            botones.add(btnEliminar);
            botones.add(btnLimpiar);

            norte.add(fila1);
            norte.add(fila2);
            norte.add(botones);
            norte.add(Box.createVerticalStrut(4));

            btnGuardar.addActionListener(e -> guardar());
            btnActualizar.addActionListener(e -> actualizar());
            btnEliminar.addActionListener(e -> eliminar());
            btnLimpiar.addActionListener(e -> limpiar());
        }

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBorder(BorderFactory.createTitledBorder("Buscar (título, autor o ISBN)"));
        busqueda.add(txtBuscar);
        JButton btnBuscar = boton("Buscar", "btnBuscarLibro");
        JButton btnTodos = boton("Mostrar todos", "btnTodosLibros");
        busqueda.add(btnBuscar);
        busqueda.add(btnTodos);
        norte.add(busqueda);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> cargarTabla(txtBuscar.getText()));
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla("");
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                cargarFormulario((Integer) tabla.getValueAt(fila, 0));
            }
        });

        if (soloLectura) {
            tabla.setToolTipText("Consulta de libros disponibles");
        }
        refrescar();
    }

    private JButton boton(String texto, String nombre) {
        JButton b = new JButton(texto);
        b.setName(nombre);
        return b;
    }

    @Override
    public void refrescar() {
        cargarCategorias();
        cargarTabla(txtBuscar.getText());
    }

    private void cargarCategorias() {
        try {
            ComboItem anterior = (ComboItem) cmbCategoria.getSelectedItem();
            cmbCategoria.removeAllItems();
            for (Categoria c : controlador.listarCategorias()) {
                cmbCategoria.addItem(new ComboItem(c.getId(), c.getNombre()));
            }
            seleccionarCategoria(anterior == null ? -1 : anterior.getId());
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void cargarTabla(String filtro) {
        try {
            cache = controlador.buscar(filtro);
            modelo.setRowCount(0);
            for (Libro l : cache) {
                modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getStock(), l.getNombreCategoria()});
            }
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private void seleccionarCategoria(int idCategoria) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getId() == idCategoria) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    private void cargarFormulario(int idLibro) {
        for (Libro l : cache) {
            if (l.getId() == idLibro) {
                txtTitulo.setText(l.getTitulo());
                txtAutor.setText(l.getAutor());
                txtIsbn.setText(l.getIsbn());
                txtEditorial.setText(l.getEditorial());
                txtStock.setText(String.valueOf(l.getStock()));
                seleccionarCategoria(l.getIdCategoria());
                return;
            }
        }
    }

    private Integer idCategoriaElegida() {
        ComboItem item = (ComboItem) cmbCategoria.getSelectedItem();
        return item == null ? null : item.getId();
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Dialogos.advertencia(this, "Seleccione un libro de la tabla.");
            return -1;
        }
        return (Integer) tabla.getValueAt(fila, 0);
    }

    private void mostrar(ResultadoOperacion r) {
        if (r.exito()) {
            Dialogos.info(this, r.mensaje());
            limpiar();
            cargarTabla(txtBuscar.getText());
        } else {
            Dialogos.advertencia(this, r.mensaje());
        }
    }

    private void guardar() {
        mostrar(controlador.guardar(txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                txtEditorial.getText(), txtStock.getText(), idCategoriaElegida()));
    }

    private void actualizar() {
        int id = idSeleccionado();
        if (id >= 0) {
            mostrar(controlador.actualizar(id, txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                    txtEditorial.getText(), txtStock.getText(), idCategoriaElegida()));
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id >= 0 && Dialogos.confirmar(this, "¿Eliminar el libro seleccionado?")) {
            mostrar(controlador.eliminar(id));
        }
    }

    private void limpiar() {
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        txtStock.setText("");
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        tabla.clearSelection();
    }
}
