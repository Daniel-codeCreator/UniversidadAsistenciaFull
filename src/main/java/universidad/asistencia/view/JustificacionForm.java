package universidad.asistencia.view;

import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.controller.JustificacionController;
import universidad.asistencia.controller.SesionClaseController;
import universidad.asistencia.enums.EstadoJustificacion;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.Justificacion;
import universidad.asistencia.model.SesionClase;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;

/** Registro y resolución de justificaciones. */
public class JustificacionForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JComboBox<Estudiante> cmbEstudiante;
    private JComboBox<SesionClase> cmbSesion;
    private JComboBox<Docente> cmbDocenteRegistra;
    private JComboBox<Docente> cmbDocenteResuelve;
    private JTextField txtMotivo;
    private JTextArea txtObservacion;
    private JComboBox<EstadoJustificacion> cmbFiltroEstado;
    private JButton btnRegistrar;
    private JButton btnAprobar;
    private JButton btnRechazar;
    private JButton btnBuscar;
    private JTextField txtBuscar;
    private JTable tblJustificaciones;
    private JButton btnSalir;

    private final JustificacionController justificacionController;
    private final EstudianteController estudianteController;
    private final SesionClaseController sesionController;
    private final DocenteController docenteController;

    public JustificacionForm() {
        this(new JustificacionController(), new EstudianteController(), new SesionClaseController(), new DocenteController());
    }

    public JustificacionForm(JustificacionController justificacionController, EstudianteController estudianteController,
                             SesionClaseController sesionController, DocenteController docenteController) {
        this.justificacionController = justificacionController;
        this.estudianteController = estudianteController;
        this.sesionController = sesionController;
        this.docenteController = docenteController;
        configurarFormulario();
        cargarCombos();
        configurarEventos();
        cargarJustificaciones();
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);
        cmbFiltroEstado.removeAllItems();
        cmbFiltroEstado.addItem(null);
        for (EstadoJustificacion estado : EstadoJustificacion.values()) cmbFiltroEstado.addItem(estado);
        ComboBoxRenderers.configurarEstudiantes(cmbEstudiante);
        ComboBoxRenderers.configurarSesiones(cmbSesion);
        ComboBoxRenderers.configurarDocentes(cmbDocenteRegistra);
        ComboBoxRenderers.configurarDocentes(cmbDocenteResuelve);
        ComboBoxRenderers.configurarEstadosJustificacion(cmbFiltroEstado);
        configurarTabla();
    }

    private void configurarTabla() {
        tblJustificaciones.setModel(new DefaultTableModel(new Object[]{"ID", "Estudiante", "Sesión", "Docente registra", "Motivo", "Estado", "Fecha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblJustificaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarCombos() {
        try {
            cmbEstudiante.removeAllItems();
            cmbEstudiante.addItem(null);
            for (Estudiante estudiante : estudianteController.listarActivos()) cmbEstudiante.addItem(estudiante);
            cmbSesion.removeAllItems();
            cmbSesion.addItem(null);
            for (SesionClase sesion : sesionController.listar()) cmbSesion.addItem(sesion);
            cmbDocenteRegistra.removeAllItems();
            cmbDocenteRegistra.addItem(null);
            cmbDocenteResuelve.removeAllItems();
            cmbDocenteResuelve.addItem(null);
            for (Docente docente : docenteController.listarActivos()) {
                cmbDocenteRegistra.addItem(docente);
                cmbDocenteResuelve.addItem(docente);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void configurarEventos() {
        btnRegistrar.addActionListener(e -> registrar());
        btnAprobar.addActionListener(e -> resolver(true));
        btnRechazar.addActionListener(e -> resolver(false));
        btnBuscar.addActionListener(e -> buscar());
        cmbFiltroEstado.addActionListener(e -> cargarJustificaciones());
        btnSalir.addActionListener(e -> salir());
        tblJustificaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarJustificacion();
        });
    }

    private void registrar() {
        try {
            Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
            SesionClase sesion = (SesionClase) cmbSesion.getSelectedItem();
            Docente docente = (Docente) cmbDocenteRegistra.getSelectedItem();
            if (estudiante == null || sesion == null || docente == null) throw new IllegalArgumentException("Seleccione estudiante, sesión y docente.");
            if (txtMotivo.getText().trim().isBlank()) throw new IllegalArgumentException("El motivo es obligatorio.");
            Justificacion justificacion = new Justificacion(estudiante, sesion, docente, txtMotivo.getText().trim(), txtObservacion.getText().trim());
            if (justificacionController.registrarJustificacion(justificacion)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Justificación registrada correctamente.");
                limpiarFormulario();
                cargarJustificaciones();
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void resolver(boolean aprobar) {
        try {
            int id = obtenerId();
            Docente docente = (Docente) cmbDocenteResuelve.getSelectedItem();
            if (docente == null) throw new IllegalArgumentException("Seleccione el docente que resuelve.");
            String accion = aprobar ? "aprobar" : "rechazar";
            if (!confirmar("¿Desea " + accion + " esta justificación?")) return;
            Justificacion resultado = aprobar
                    ? justificacionController.aprobarJustificacion(id, docente.getIdDocente())
                    : justificacionController.rechazarJustificacion(id, docente.getIdDocente());
            JOptionPane.showMessageDialog(panelPrincipal, "Justificación " + resultado.getEstado().name().toLowerCase() + ".");
            cargarJustificaciones();
            seleccionarFilaPorId(id);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isBlank()) {
            cargarJustificaciones();
            return;
        }
        try {
            int id = Integer.parseInt(texto);
            justificacionController.buscar(id).ifPresentOrElse(this::mostrarJustificacion,
                    () -> JOptionPane.showMessageDialog(panelPrincipal, "No se encontró la justificación."));
        } catch (RuntimeException e) {
            mostrarError("Ingrese un ID de justificación válido.");
        }
    }

    private void cargarJustificaciones() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblJustificaciones.getModel();
            modelo.setRowCount(0);
            EstadoJustificacion filtro = (EstadoJustificacion) cmbFiltroEstado.getSelectedItem();
            java.util.List<Justificacion> lista = filtro == null
                    ? justificacionController.listar()
                    : justificacionController.listarPorEstado(filtro);
            for (Justificacion justificacion : lista) {
                modelo.addRow(new Object[]{justificacion.getIdJustificacion(), nombreEstudiante(justificacion.getEstudiante()),
                        nombreSesion(justificacion.getSesionClase()), nombreDocente(justificacion.getDocenteRegistra()),
                        justificacion.getMotivo(), ComboBoxRenderers.texto(justificacion.getEstado()),
                        ComboBoxRenderers.fechaHora(justificacion.getFecha())});
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void seleccionarJustificacion() {
        int fila = tblJustificaciones.getSelectedRow();
        if (fila < 0) return;
        try {
            justificacionController.buscar(Integer.parseInt(tblJustificaciones.getValueAt(fila, 0).toString()))
                    .ifPresent(this::mostrarJustificacion);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarJustificacion(Justificacion justificacion) {
        txtId.setText(String.valueOf(justificacion.getIdJustificacion()));
        seleccionar(cmbEstudiante, justificacion.getEstudiante());
        seleccionar(cmbSesion, justificacion.getSesionClase());
        seleccionar(cmbDocenteRegistra, justificacion.getDocenteRegistra());
        txtMotivo.setText(justificacion.getMotivo());
        txtObservacion.setText(justificacion.getObservacion() == null ? "" : justificacion.getObservacion());
        boolean pendiente = justificacion.getEstado() == EstadoJustificacion.PENDIENTE;
        btnAprobar.setEnabled(pendiente);
        btnRechazar.setEnabled(pendiente);
        btnRegistrar.setEnabled(false);
    }

    private void seleccionar(JComboBox<?> combo, Object buscado) {
        if (buscado == null) return;
        int id = buscado instanceof Estudiante estudiante ? estudiante.getIdEstudiante()
                : buscado instanceof SesionClase sesion ? sesion.getIdSesion() : ((Docente) buscado).getIdDocente();
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item == null) continue;
            int itemId = item instanceof Estudiante estudiante ? estudiante.getIdEstudiante()
                    : item instanceof SesionClase sesion ? sesion.getIdSesion() : ((Docente) item).getIdDocente();
            if (itemId == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String nombreEstudiante(Estudiante estudiante) {
        return estudiante == null ? "" : estudiante.getCarnet() + " - " + estudiante.getNombres() + " " + estudiante.getApellidos();
    }

    private String nombreSesion(SesionClase sesion) {
        return sesion == null ? "" : sesion.getFecha() + " / " + (sesion.getSeccion() == null ? "" : sesion.getSeccion().getCodigo());
    }

    private String nombreDocente(Docente docente) {
        return docente == null ? "" : docente.getCodigoEmpleado() + " - " + docente.getNombres() + " " + docente.getApellidos();
    }

    private int obtenerId() {
        if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione una justificación.");
        return Integer.parseInt(txtId.getText().trim());
    }

    private void seleccionarFilaPorId(int id) {
        for (int fila = 0; fila < tblJustificaciones.getRowCount(); fila++) {
            if (Integer.parseInt(tblJustificaciones.getValueAt(fila, 0).toString()) == id) {
                tblJustificaciones.setRowSelectionInterval(fila, fila);
                tblJustificaciones.scrollRectToVisible(tblJustificaciones.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtMotivo.setText("");
        txtObservacion.setText("");
        txtBuscar.setText("");
        if (cmbEstudiante.getItemCount() > 0) cmbEstudiante.setSelectedIndex(0);
        if (cmbSesion.getItemCount() > 0) cmbSesion.setSelectedIndex(0);
        if (cmbDocenteRegistra.getItemCount() > 0) cmbDocenteRegistra.setSelectedIndex(0);
        if (cmbDocenteResuelve.getItemCount() > 0) cmbDocenteResuelve.setSelectedIndex(0);
        tblJustificaciones.clearSelection();
        btnRegistrar.setEnabled(true);
        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(panelPrincipal, mensaje, "Confirmar resolución", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(panelPrincipal,
                mensaje == null || mensaje.isBlank() ? "Ocurrió un error inesperado." : mensaje,
                "Justificaciones", JOptionPane.ERROR_MESSAGE);
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
