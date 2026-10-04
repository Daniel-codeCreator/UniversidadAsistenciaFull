package universidad.asistencia.view;

import com.toedter.calendar.JDateChooser;
import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.controller.InscripcionController;
import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.Inscripcion;
import universidad.asistencia.model.Seccion;
import universidad.asistencia.util.DateUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.time.LocalDate;

/** Administración de inscripciones de estudiantes en secciones. */
public class InscripcionForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JComboBox<Estudiante> cmbEstudiante;
    private JComboBox<Seccion> cmbSeccion;
    private JDateChooser dcFechaInscripcion;
    private JDateChooser dcFechaRetiro;
    private JCheckBox chkActiva;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnRetirar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblInscripciones;
    private JButton btnSalir;

    private final InscripcionController inscripcionController;
    private final EstudianteController estudianteController;
    private final SeccionController seccionController;

    public InscripcionForm() {
        this(new InscripcionController(), new EstudianteController(), new SeccionController());
    }

    public InscripcionForm(InscripcionController inscripcionController,
                           EstudianteController estudianteController,
                           SeccionController seccionController) {
        this.inscripcionController = inscripcionController;
        this.estudianteController = estudianteController;
        this.seccionController = seccionController;
        configurarFormulario();
        cargarCombos();
        configurarEventos();
        cargarInscripciones();
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        chkActiva.setSelected(true);
        chkActiva.setEnabled(false);
        btnActualizar.setEnabled(false);
        btnRetirar.setEnabled(false);
        dcFechaInscripcion.setDateFormatString("dd/MM/yyyy");
        dcFechaRetiro.setDateFormatString("dd/MM/yyyy");
        dcFechaInscripcion.setDate(DateUtils.toDate(LocalDate.now()));
        configurarTabla();
    }

    private void configurarTabla() {
        tblInscripciones.setModel(new DefaultTableModel(
                new Object[]{"ID", "Estudiante", "Sección", "Fecha inscripción", "Fecha retiro", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblInscripciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarCombos() {
        try {
            cmbEstudiante.removeAllItems();
            cmbEstudiante.addItem(null);
            for (Estudiante estudiante : estudianteController.listarActivos()) cmbEstudiante.addItem(estudiante);

            cmbSeccion.removeAllItems();
            cmbSeccion.addItem(null);
            for (Seccion seccion : seccionController.listarActivos()) cmbSeccion.addItem(seccion);

            ComboBoxRenderers.configurarEstudiantes(cmbEstudiante);
            ComboBoxRenderers.configurarSecciones(cmbSeccion);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnRetirar.addActionListener(e -> retirar());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());
        tblInscripciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarInscripcion();
        });
    }

    private void guardar() {
        try {
            if (inscripcionController.guardar(construirInscripcion(0, true))) {
                JOptionPane.showMessageDialog(panelPrincipal, "Inscripción guardada correctamente.");
                limpiarFormulario();
                cargarInscripciones();
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void actualizar() {
        try {
            int id = obtenerId();
            if (!confirmar("¿Desea actualizar esta inscripción?")) return;
            if (inscripcionController.actualizar(construirInscripcion(id, chkActiva.isSelected()))) {
                JOptionPane.showMessageDialog(panelPrincipal, "Inscripción actualizada correctamente.");
                cargarInscripciones();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void retirar() {
        try {
            int id = obtenerId();
            if (!confirmar("¿Desea retirar esta inscripción?")) return;
            LocalDate fecha = leerFecha(dcFechaRetiro);
            if (inscripcionController.retirar(id, fecha)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Inscripción retirada correctamente.");
                cargarInscripciones();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private Inscripcion construirInscripcion(int id, boolean activa) {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Seccion seccion = (Seccion) cmbSeccion.getSelectedItem();
        if (estudiante == null || seccion == null) throw new IllegalArgumentException("Seleccione estudiante y sección.");
        return new Inscripcion(id, estudiante, seccion, leerFecha(dcFechaInscripcion),
                fechaOpcional(dcFechaRetiro), activa);
    }

    private void cargarInscripciones() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblInscripciones.getModel();
            modelo.setRowCount(0);
            for (Inscripcion inscripcion : inscripcionController.listar()) {
                modelo.addRow(new Object[]{inscripcion.getIdInscripcion(), nombreEstudiante(inscripcion),
                        nombreSeccion(inscripcion), ComboBoxRenderers.fecha(inscripcion.getFechaInscripcion()),
                        ComboBoxRenderers.fecha(inscripcion.getFechaRetiro()),
                        inscripcion.isActiva() ? "ACTIVA" : "RETIRADA"});
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isBlank()) {
            cargarInscripciones();
            return;
        }
        try {
            int id = Integer.parseInt(texto);
            inscripcionController.buscar(id).ifPresentOrElse(this::mostrarInscripcion,
                    () -> JOptionPane.showMessageDialog(panelPrincipal, "No se encontró la inscripción."));
        } catch (RuntimeException e) {
            mostrarError("Ingrese un ID de inscripción válido.");
        }
    }

    private void seleccionarInscripcion() {
        int fila = tblInscripciones.getSelectedRow();
        if (fila < 0) return;
        try {
            inscripcionController.buscar(Integer.parseInt(tblInscripciones.getValueAt(fila, 0).toString()))
                    .ifPresent(this::mostrarInscripcion);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarInscripcion(Inscripcion inscripcion) {
        txtId.setText(String.valueOf(inscripcion.getIdInscripcion()));
        seleccionar(cmbEstudiante, inscripcion.getEstudiante());
        seleccionar(cmbSeccion, inscripcion.getSeccion());
        dcFechaInscripcion.setDate(DateUtils.toDate(inscripcion.getFechaInscripcion()));
        dcFechaRetiro.setDate(DateUtils.toDate(inscripcion.getFechaRetiro()));
        chkActiva.setSelected(inscripcion.isActiva());
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnRetirar.setEnabled(inscripcion.isActiva());
    }

    private void seleccionar(JComboBox<?> combo, Object buscado) {
        if (buscado == null) return;
        int id = buscado instanceof Estudiante estudiante ? estudiante.getIdEstudiante() : ((Seccion) buscado).getIdSeccion();
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item == null) continue;
            int itemId = item instanceof Estudiante estudiante ? estudiante.getIdEstudiante() : ((Seccion) item).getIdSeccion();
            if (itemId == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String nombreEstudiante(Inscripcion inscripcion) {
        Estudiante estudiante = inscripcion.getEstudiante();
        return estudiante == null ? "" : estudiante.getCarnet() + " - " + estudiante.getNombres() + " " + estudiante.getApellidos();
    }

    private String nombreSeccion(Inscripcion inscripcion) {
        Seccion seccion = inscripcion.getSeccion();
        return seccion == null ? "" : seccion.getCodigo() + " - " + (seccion.getCurso() == null ? "" : seccion.getCurso().getNombre());
    }

    private LocalDate leerFecha(JDateChooser campo) {
        if (campo.getDate() == null) throw new IllegalArgumentException("Seleccione una fecha válida.");
        return DateUtils.toLocalDate(campo.getDate());
    }

    private LocalDate fechaOpcional(JDateChooser campo) {
        return campo.getDate() == null ? null : leerFecha(campo);
    }

    private int obtenerId() {
        if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione una inscripción.");
        return Integer.parseInt(txtId.getText().trim());
    }

    private void seleccionarFilaPorId(int id) {
        for (int fila = 0; fila < tblInscripciones.getRowCount(); fila++) {
            if (Integer.parseInt(tblInscripciones.getValueAt(fila, 0).toString()) == id) {
                tblInscripciones.setRowSelectionInterval(fila, fila);
                tblInscripciones.scrollRectToVisible(tblInscripciones.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void nuevo() {
        limpiarFormulario();
        dcFechaInscripcion.setDate(DateUtils.toDate(LocalDate.now()));
    }

    private void limpiarFormulario() {
        txtId.setText("");
        dcFechaInscripcion.setDate(DateUtils.toDate(LocalDate.now()));
        dcFechaRetiro.setDate(null);
        txtBuscar.setText("");
        chkActiva.setSelected(true);
        if (cmbEstudiante.getItemCount() > 0) cmbEstudiante.setSelectedIndex(0);
        if (cmbSeccion.getItemCount() > 0) cmbSeccion.setSelectedIndex(0);
        tblInscripciones.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnRetirar.setEnabled(false);
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(panelPrincipal, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void createUIComponents() {
        dcFechaInscripcion = new JDateChooser();
        dcFechaRetiro = new JDateChooser();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(panelPrincipal,
                mensaje == null || mensaje.isBlank() ? "Ocurrió un error inesperado." : mensaje,
                "Inscripciones", JOptionPane.ERROR_MESSAGE);
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            Window ventana = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (ventana != null) ventana.dispose();
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}
