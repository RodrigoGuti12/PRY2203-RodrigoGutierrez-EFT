package vista;

import controlador.CategoriaController;
import controlador.EstudianteController;
import controlador.LibroController;
import controlador.OperacionException;
import controlador.PrestamoController;
import controlador.ReporteController;
import modelo.Estudiante;
import modelo.Modulo;
import modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal. El menú de navegación lateral se arma con los módulos que el
 * propio objeto Usuario declara (polimorfismo): bibliotecario y estudiante ven distinto.
 */
public class MainFrame extends JFrame {

    private final Usuario usuario;
    private final Estudiante estudiante; // ficha del estudiante (null para bibliotecario)
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Map<Modulo, Refrescable> paneles = new EnumMap<>(Modulo.class);

    public MainFrame(Usuario usuario, Estudiante estudiante) throws OperacionException {
        super("Biblioteca Escolar");
        this.usuario = usuario;
        this.estudiante = estudiante;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel cabecera = new JLabel("  Usuario: " + usuario.getNombre() + "  |  Rol: " + usuario.getRol().getValorBd());
        cabecera.setName("lblUsuario");
        cabecera.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        cabecera.setFont(cabecera.getFont().deriveFont(java.awt.Font.BOLD));
        add(cabecera, BorderLayout.NORTH);

        JPanel navegacion = new JPanel();
        navegacion.setLayout(new BoxLayout(navegacion, BoxLayout.Y_AXIS));
        navegacion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ButtonGroup grupo = new ButtonGroup();

        for (Modulo m : Modulo.values()) { // orden fijo del enum
            if (!usuario.getModulosPermitidos().contains(m)) {
                continue;
            }
            Component panel = crearPanel(m);
            contenido.add(panel, m.name());
            paneles.put(m, (Refrescable) panel);

            JToggleButton b = new JToggleButton(m.getTitulo());
            b.setName("nav" + m.name());
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(190, 36));
            b.setPreferredSize(new Dimension(190, 36));
            b.addActionListener(e -> mostrar(m));
            grupo.add(b);
            navegacion.add(b);
            navegacion.add(javax.swing.Box.createVerticalStrut(6));
            if (grupo.getButtonCount() == 1) {
                b.setSelected(true);
                cartas.show(contenido, m.name());
            }
        }

        add(navegacion, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        JMenuBar barra = new JMenuBar();
        JMenu menu = new JMenu("Sesión");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        JMenuItem salir = new JMenuItem("Salir");
        cerrar.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        salir.addActionListener(e -> System.exit(0));
        menu.add(cerrar);
        menu.add(salir);
        barra.add(menu);
        setJMenuBar(barra);
    }

    private Component crearPanel(Modulo m) throws OperacionException {
        Estudiante fijo = estudiante;
        return switch (m) {
            case LIBROS -> new PanelLibros(new LibroController(), false);
            case CONSULTA_LIBROS -> new PanelLibros(new LibroController(), true);
            case CATEGORIAS -> new PanelCategorias(new CategoriaController());
            case ESTUDIANTES -> new PanelEstudiantes(new EstudianteController());
            case PRESTAMOS -> new PanelPrestamos(prestamos(), fijo);
            case REPORTES -> new PanelReportes(new ReporteController(), listaEstudiantes(), null);
            case MI_HISTORIAL -> new PanelReportes(new ReporteController(), Collections.emptyList(), fijo);
        };
    }

    private PrestamoController prestamoCtrl;

    private PrestamoController prestamos() {
        if (prestamoCtrl == null) {
            prestamoCtrl = new PrestamoController();
        }
        return prestamoCtrl;
    }

    private List<Estudiante> listaEstudiantes() throws OperacionException {
        return new EstudianteController().listar();
    }

    private void mostrar(Modulo m) {
        paneles.get(m).refrescar();
        cartas.show(contenido, m.name());
    }
}
