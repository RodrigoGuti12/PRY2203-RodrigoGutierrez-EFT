package vista;

import controlador.EstudianteController;
import controlador.OperacionException;
import modelo.Estudiante;
import modelo.ResultadoOperacion;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Pantalla CRUD de estudiantes (también crea/actualiza su acceso al sistema). */
public class PanelEstudiantes extends JPanel implements Refrescable {

    private final EstudianteController controlador;

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtRut = new JTextField(11);
    private final JTextField txtCurso = new JTextField(10);
    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(12);

    private final DefaultTableModel modelo = Tablas.modelo("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = Tablas.tabla(modelo, "tablaEstudiantes");

    public PanelEstudiantes(EstudianteController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtNombre.setName("txtNombreEstudiante");
        txtRut.setName("txtRutEstudiante");
        txtCurso.setName("txtCursoEstudiante");
        txtCorreo.setName("txtCorreoEstudiante");
        txtClave.setName("txtClaveEstudiante");

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.add(new JLabel("Nombre:"));
        fila1.add(txtNombre);
        fila1.add(new JLabel("RUT (12345678-9):"));
        fila1.add(txtRut);
        fila1.add(new JLabel("Curso:"));
        fila1.add(txtCurso);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.add(new JLabel("Correo:"));
        fila2.add(txtCorreo);
        fila2.add(new JLabel("Contraseña de acceso:"));
        fila2.add(txtClave);
        JLabel ayuda = new JLabel("(al actualizar, vacía = no cambia)");
        fila2.add(ayuda);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnGuardar = boton("Guardar", "btnGuardarEstudiante");
        JButton btnActualizar = boton("Actualizar", "btnActualizarEstudiante");
        JButton btnEliminar = boton("Eliminar", "btnEliminarEstudiante");
        JButton btnLimpiar = boton("Limpiar", "btnLimpiarEstudiante");
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.add(fila1);
        norte.add(fila2);
        norte.add(botones);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                txtNombre.setText(String.valueOf(tabla.getValueAt(fila, 1)));
                txtRut.setText(String.valueOf(tabla.getValueAt(fila, 2)));
                txtCurso.setText(String.valueOf(tabla.getValueAt(fila, 3)));
                txtCorreo.setText(String.valueOf(tabla.getValueAt(fila, 4)));
                txtClave.setText("");
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
            for (Estudiante e : controlador.listar()) {
                modelo.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
            }
        } catch (OperacionException ex) {
            Dialogos.error(this, ex.getMessage());
        }
    }

    private int idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Dialogos.advertencia(this, "Seleccione un estudiante de la tabla.");
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
        mostrar(controlador.guardar(txtNombre.getText(), txtRut.getText(), txtCurso.getText(),
                txtCorreo.getText(), new String(txtClave.getPassword())));
    }

    private void actualizar() {
        int id = idSeleccionado();
        if (id >= 0) {
            mostrar(controlador.actualizar(id, txtNombre.getText(), txtRut.getText(), txtCurso.getText(),
                    txtCorreo.getText(), new String(txtClave.getPassword())));
        }
    }

    private void eliminar() {
        int id = idSeleccionado();
        if (id >= 0 && Dialogos.confirmar(this, "¿Eliminar el estudiante seleccionado y su acceso al sistema?")) {
            mostrar(controlador.eliminar(id));
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        txtClave.setText("");
        tabla.clearSelection();
    }
}
