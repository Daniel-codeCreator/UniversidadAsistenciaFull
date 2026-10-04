package universidad.asistencia.view;

import universidad.asistencia.model.Usuario;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;

/** Menú principal de la aplicación. */
public class MainForm {

    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private JButton btnEstudiantes;
    private JButton btnDocentes;
    private JButton btnCursos;
    private JButton btnPeriodos;
    private JButton btnSecciones;
    private JButton btnHorarios;
    private JButton btnInscripciones;
    private JButton btnSesiones;
    private JButton btnDispositivos;
    private JButton btnAsistencia;
    private JButton btnJustificaciones;
    private JButton btnUsuarios;
    private JButton btnSalir;
    private final Usuario usuarioAutenticado;

    public MainForm() {
        this(null);
    }

    public MainForm(Usuario usuarioAutenticado) {
        this.usuarioAutenticado = usuarioAutenticado;
        configurarFormulario();
        configurarEventos();
    }

    private void configurarFormulario() {
        lblTitulo.setText("SISTEMA DE ASISTENCIA UNIVERSITARIA");
    }

    private void configurarEventos() {
        btnEstudiantes.addActionListener(e -> abrirEstudiantes());
        btnDocentes.addActionListener(e -> abrirDocentes());
        btnCursos.addActionListener(e -> abrirCursos());
        btnPeriodos.addActionListener(e -> abrirPeriodos());
        btnSecciones.addActionListener(e -> abrirSecciones());
        btnHorarios.addActionListener(e -> abrirHorarios());
        btnInscripciones.addActionListener(e -> abrirInscripciones());
        btnSesiones.addActionListener(e -> abrirSesiones());
        btnDispositivos.addActionListener(e -> abrirDispositivos());
        btnAsistencia.addActionListener(e -> abrirAsistencia());
        btnJustificaciones.addActionListener(e -> abrirJustificaciones());
        btnUsuarios.addActionListener(e -> abrirUsuarios());
        btnSalir.addActionListener(e -> salir());
    }

    private void abrirEstudiantes() {
        EstudianteView view = new EstudianteView();
        abrirVentana("Administración de Estudiantes", view.getPanelPrincipal(), 950, 650);
    }

    private void abrirDocentes() {
        DocenteForm view = new DocenteForm();
        abrirVentana("Administración de Docentes", view.getPanelPrincipal(), 950, 650);
    }

    private void abrirCursos() {
        CursoView view = new CursoView();
        abrirVentana("Administración de Cursos", view.getPanelPrincipal(), 950, 650);
    }

    private void abrirPeriodos() {
        PeriodoAcademicoForm view = new PeriodoAcademicoForm();
        abrirVentana("Administración de Periodos Académicos", view.getPanelPrincipal(), 950, 650);
    }

    private void abrirSecciones() {
        SeccionForm view = new SeccionForm();
        abrirVentana("Administración de Secciones", view.getPanelPrincipal(), 1050, 680);
    }

    private void abrirHorarios() {
        HorarioSemanalForm view = new HorarioSemanalForm();
        abrirVentana("Administración de Horarios", view.getPanelPrincipal(), 900, 600);
    }

    private void abrirInscripciones() {
        InscripcionForm view = new InscripcionForm();
        abrirVentana("Administración de Inscripciones", view.getPanelPrincipal(), 1050, 650);
    }

    private void abrirSesiones() {
        SesionClaseForm view = new SesionClaseForm();
        abrirVentana("Administración de Sesiones", view.getPanelPrincipal(), 1050, 680);
    }

    private void abrirDispositivos() {
        DispositivoForm view = new DispositivoForm();
        abrirVentana("Administración de Dispositivos", view.getPanelPrincipal(), 950, 650);
    }

    private void abrirAsistencia() {
        AsistenciaForm view = new AsistenciaForm();
        abrirVentana("Control de Asistencia y Marcajes", view.getPanelPrincipal(), 1100, 700);
    }

    private void abrirJustificaciones() {
        JustificacionForm view = new JustificacionForm();
        abrirVentana("Administración de Justificaciones", view.getPanelPrincipal(), 1100, 700);
    }

    private void abrirUsuarios() {
        UsuarioForm view = new UsuarioForm(usuarioAutenticado);
        abrirVentana("Administración de Usuarios", view.getPanelPrincipal(), 1050, 650);
    }

    /** Abre cualquier catálogo dentro de una ventana secundaria. */
    private void abrirVentana(String titulo, JPanel panel, int ancho, int alto) {
        JFrame ventana = new JFrame(titulo);
        ventana.setContentPane(panel);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        int maxAncho = Math.max(640, pantalla.width - 40);
        int maxAlto = Math.max(480, pantalla.height - 80);
        Dimension minimo = new Dimension(Math.min(ancho, maxAncho), Math.min(alto, maxAlto));
        ventana.setMinimumSize(minimo);
        ventana.pack();
        int anchoFinal = Math.min(Math.max(ventana.getWidth(), minimo.width), maxAncho);
        int altoFinal = Math.min(Math.max(ventana.getHeight(), minimo.height), maxAlto);
        ventana.setSize(anchoFinal, altoFinal);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(
                panelPrincipal,
                "¿Desea salir del sistema?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}
