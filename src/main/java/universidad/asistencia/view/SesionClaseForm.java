package universidad.asistencia.view;

import com.toedter.calendar.JDateChooser;
import universidad.asistencia.controller.HorarioSemanalController;
import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.controller.SesionClaseController;
import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.model.Seccion;
import universidad.asistencia.model.SesionClase;
import universidad.asistencia.util.DateUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Administración de sesiones de clase. */
public class SesionClaseForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JComboBox<Seccion> cmbSeccion;
    private JComboBox<HorarioSemanal> cmbHorario;
    private JDateChooser dcFecha;
    private JSpinner spnHoraInicio;
    private JSpinner spnHoraFin;
    private JTextField txtAula;
    private JComboBox<EstadoSesion> cmbEstado;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnCambiarEstado;
    private JButton btnGenerar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblSesiones;
    private JButton btnSalir;

    private final SesionClaseController sesionController;
    private final SeccionController seccionController;
    private final HorarioSemanalController horarioController;

    public SesionClaseForm() {
        this(new SesionClaseController(), new SeccionController(), new HorarioSemanalController());
    }

    public SesionClaseForm(SesionClaseController sesionController, SeccionController seccionController,
                           HorarioSemanalController horarioController) {
        this.sesionController = sesionController;
        this.seccionController = seccionController;
        this.horarioController = horarioController;
        configurarFormulario();
        cargarCombos();
        configurarEventos();
        cargarSesiones();
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        btnActualizar.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
        dcFecha.setDateFormatString("dd/MM/yyyy");
        dcFecha.setDate(DateUtils.toDate(LocalDate.now()));
        configurarSpinnerHora(spnHoraInicio, LocalTime.of(7, 0));
        configurarSpinnerHora(spnHoraFin, LocalTime.of(8, 0));
        cmbEstado.removeAllItems();
        cmbEstado.addItem(null);
        for (EstadoSesion estado : EstadoSesion.values()) cmbEstado.addItem(estado);
        cmbEstado.setSelectedItem(EstadoSesion.PROGRAMADA);
        ComboBoxRenderers.configurarEstadosSesion(cmbEstado);
        ComboBoxRenderers.configurarSecciones(cmbSeccion);
        ComboBoxRenderers.configurarHorarios(cmbHorario);
        configurarTabla();
    }

    private void configurarSpinnerHora(JSpinner spinner, LocalTime valorInicial) {
        spinner.setModel(new SpinnerDateModel(toDate(valorInicial), null, null, Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
    }

    private void configurarTabla() {
        tblSesiones.setModel(new DefaultTableModel(new Object[]{"ID", "Sección", "Fecha", "Inicio", "Fin", "Aula", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblSesiones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarCombos() {
        try {
            cmbSeccion.removeAllItems();
            cmbSeccion.addItem(null);
            for (Seccion seccion : seccionController.listarActivos()) cmbSeccion.addItem(seccion);
            cargarHorariosDeSeccion();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarHorariosDeSeccion() {
        cmbHorario.removeAllItems();
        cmbHorario.addItem(null);
        Seccion seccion = (Seccion) cmbSeccion.getSelectedItem();
        if (seccion == null) return;
        try {
            for (HorarioSemanal horario : horarioController.listarPorSeccion(seccion.getIdSeccion())) {
                cmbHorario.addItem(horario);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void configurarEventos() {
        cmbSeccion.addActionListener(e -> cargarHorariosDeSeccion());
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnGenerar.addActionListener(e -> generarSesiones());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());
        tblSesiones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarSesion();
        });
    }

    private void guardar() {
        try {
            if (sesionController.guardar(construirSesion(0))) {
                JOptionPane.showMessageDialog(panelPrincipal, "Sesión guardada correctamente.");
                limpiarFormulario();
                cargarSesiones();
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void actualizar() {
        try {
            int id = obtenerId();
            if (!confirmar("¿Desea actualizar esta sesión?")) return;
            if (sesionController.actualizar(construirSesion(id))) {
                JOptionPane.showMessageDialog(panelPrincipal, "Sesión actualizada correctamente.");
                cargarSesiones();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cambiarEstado() {
        try {
            int id = obtenerId();
            EstadoSesion estado = (EstadoSesion) cmbEstado.getSelectedItem();
            if (estado == null) throw new IllegalArgumentException("Seleccione un estado.");
            if (!confirmar("¿Desea cambiar el estado de esta sesión a " + estado.name().toLowerCase() + "?")) return;
            if (sesionController.cambiarEstado(id, estado)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Estado actualizado correctamente.");
                cargarSesiones();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private SesionClase construirSesion(int id) {
        Seccion seccion = (Seccion) cmbSeccion.getSelectedItem();
        EstadoSesion estado = (EstadoSesion) cmbEstado.getSelectedItem();
        if (seccion == null) throw new IllegalArgumentException("Seleccione una sección.");
        if (estado == null) throw new IllegalArgumentException("Seleccione un estado.");
        if (txtAula.getText().trim().isBlank()) throw new IllegalArgumentException("El aula es obligatoria.");
        LocalTime inicio = leerHora(spnHoraInicio);
        LocalTime fin = leerHora(spnHoraFin);
        if (!fin.isAfter(inicio)) throw new IllegalArgumentException("La hora fin debe ser posterior a la hora inicio.");
        return new SesionClase(id, seccion, leerFecha(), inicio, fin, txtAula.getText().trim(), estado);
    }

    private void generarSesiones() {
        try {
            Seccion seccion = (Seccion) cmbSeccion.getSelectedItem();
            HorarioSemanal horario = (HorarioSemanal) cmbHorario.getSelectedItem();
            if (seccion == null || horario == null) throw new IllegalArgumentException("Seleccione una sección con horario semanal.");
            if (!confirmar("¿Desea generar las sesiones del periodo seleccionado?")) return;
            List<SesionClase> creadas = sesionController.generarSesiones(seccion.getPeriodoAcademico(), seccion, horario);
            JOptionPane.showMessageDialog(panelPrincipal, "Sesiones generadas: " + creadas.size());
            cargarSesiones();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarSesiones() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblSesiones.getModel();
            modelo.setRowCount(0);
            for (SesionClase sesion : sesionController.listar()) {
                modelo.addRow(new Object[]{sesion.getIdSesion(), nombreSeccion(sesion.getSeccion()), ComboBoxRenderers.fecha(sesion.getFecha()),
                        ComboBoxRenderers.hora(sesion.getHoraInicioProgramada()), ComboBoxRenderers.hora(sesion.getHoraFinProgramada()),
                        sesion.getAula(), ComboBoxRenderers.texto(sesion.getEstado())});
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isBlank()) {
            cargarSesiones();
            return;
        }
        try {
            int id = Integer.parseInt(texto);
            sesionController.buscar(id).ifPresentOrElse(this::mostrarSesion,
                    () -> JOptionPane.showMessageDialog(panelPrincipal, "No se encontró la sesión."));
        } catch (RuntimeException e) {
            mostrarError("Ingrese un ID de sesión válido.");
        }
    }

    private void seleccionarSesion() {
        int fila = tblSesiones.getSelectedRow();
        if (fila < 0) return;
        try {
            sesionController.buscar(Integer.parseInt(tblSesiones.getValueAt(fila, 0).toString()))
                    .ifPresent(this::mostrarSesion);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarSesion(SesionClase sesion) {
        txtId.setText(String.valueOf(sesion.getIdSesion()));
        seleccionarSeccion(sesion.getSeccion());
        dcFecha.setDate(DateUtils.toDate(sesion.getFecha()));
        spnHoraInicio.setValue(toDate(sesion.getHoraInicioProgramada()));
        spnHoraFin.setValue(toDate(sesion.getHoraFinProgramada()));
        txtAula.setText(sesion.getAula());
        cmbEstado.setSelectedItem(sesion.getEstado());
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnCambiarEstado.setEnabled(true);
    }

    private void seleccionarSeccion(Seccion buscada) {
        if (buscada == null) return;
        for (int i = 0; i < cmbSeccion.getItemCount(); i++) {
            Seccion actual = cmbSeccion.getItemAt(i);
            if (actual != null && actual.getIdSeccion() == buscada.getIdSeccion()) {
                cmbSeccion.setSelectedIndex(i);
                return;
            }
        }
        cmbSeccion.addItem(buscada);
        cmbSeccion.setSelectedItem(buscada);
    }

    private LocalDate leerFecha() {
        if (dcFecha.getDate() == null) throw new IllegalArgumentException("Seleccione una fecha válida.");
        return DateUtils.toLocalDate(dcFecha.getDate());
    }

    private LocalTime leerHora(JSpinner spinner) {
        Date valor = (Date) spinner.getValue();
        return valor.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private Date toDate(LocalTime hora) {
        return Date.from(hora.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }

    private String nombreSeccion(Seccion seccion) {
        return seccion == null ? "" : seccion.getCodigo() + " - " + (seccion.getCurso() == null ? "" : seccion.getCurso().getNombre());
    }

    private int obtenerId() {
        if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione una sesión.");
        return Integer.parseInt(txtId.getText().trim());
    }

    private void seleccionarFilaPorId(int id) {
        for (int fila = 0; fila < tblSesiones.getRowCount(); fila++) {
            if (Integer.parseInt(tblSesiones.getValueAt(fila, 0).toString()) == id) {
                tblSesiones.setRowSelectionInterval(fila, fila);
                tblSesiones.scrollRectToVisible(tblSesiones.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void nuevo() {
        limpiarFormulario();
        dcFecha.setDate(DateUtils.toDate(LocalDate.now()));
    }

    private void limpiarFormulario() {
        txtId.setText("");
        dcFecha.setDate(DateUtils.toDate(LocalDate.now()));
        txtAula.setText("");
        txtBuscar.setText("");
        configurarSpinnerHora(spnHoraInicio, LocalTime.of(7, 0));
        configurarSpinnerHora(spnHoraFin, LocalTime.of(8, 0));
        if (cmbSeccion.getItemCount() > 0) cmbSeccion.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedItem(EstadoSesion.PROGRAMADA);
        tblSesiones.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(panelPrincipal, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void createUIComponents() {
        dcFecha = new JDateChooser();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(panelPrincipal,
                mensaje == null || mensaje.isBlank() ? "Ocurrió un error inesperado." : mensaje,
                "Sesiones", JOptionPane.ERROR_MESSAGE);
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
