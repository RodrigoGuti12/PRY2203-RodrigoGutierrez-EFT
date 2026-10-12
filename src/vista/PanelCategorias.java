package vista;

import controlador.CategoriaController;
import controlador.OperacionException;
import modelo.Categoria;
import modelo.ResultadoOperacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Pantalla CRUD de categorías de libros. */
public class PanelCategorias extends JPanel implements Refrescable {

    private final CategoriaController controlador;
    private final JTextField txtNombre = new JTextField(25);
    private final DefaultTableModel modelo = Tablas.modelo("ID", "Nombre");
    private final JTable tabla = Tablas.tabla(modelo, "tablaCategorias");

    public PanelCategorias(CategoriaController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtNombre.setName("txtNombreCategoria");

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        JButton btnGuardar = boton("Guardar", "btnGuardarCategoria");
        JButton btnActualizar = boton("Actualizar", "btnActualizarCategoria");
        JButton btnEliminar = boton("Eliminar", "btnEliminarCategoria");
        JButton btnLimpiar = boton("Limpiar", "btnLimpiarCategoria");
        formulario.add(btnGuardar);
        formulario.add(btnActualizar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                txtNombre.setText(String.valueOf(tabla.getValueAt(fila, 1)));
            }
        });
        refrescar();
    }

    private JButton boton(String texto, String nombre) {
        JButton b = new JButton(texto);
        b.setName(nombre);
        return b;
    }

    @Override
    public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Categoria c : controlador.listar()) {
                modelo.addRow(new Object[]{c.getId(), c.getNombre()});
            }
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Dialogos.advertencia(this, "Seleccione una categoría de la tabla.");
            return -1;
        }
        return (Integer) tabla.getValueAt(fila, 0);
    }

    private void mostrar(ResultadoOperacion r) {
        if (r.exito()) {
            Dialogos.info(this, r.mensaje());
            limpiar();
            refrescar();
        } else {
            Dialogos.advertencia(this, r.mensaje());
        }
    }

    private void guardar() {
        mostrar(controlador.guardar(txtNombre.getText()));
    }

    private void actualizar() {
        int id = idSeleccionado();
        if (id >= 0) {
            mostrar(controlador.actualizar(id, txtNombre.getText()));
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id >= 0 && Dialogos.confirmar(this, "¿Eliminar la categoría seleccionada?")) {
            mostrar(controlador.eliminar(id));
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        tabla.clearSelection();
    }
}
