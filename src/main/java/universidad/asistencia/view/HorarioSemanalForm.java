package universidad.asistencia.view;

import universidad.asistencia.controller.HorarioSemanalController;
import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.model.Seccion;

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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/** Administración de horarios semanales. */
public class HorarioSemanalForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JComboBox<Seccion> cmbSeccion;
    private JComboBox<String> cmbDia;
    private JSpinner spnHoraInicio;
    private JSpinner spnHoraFin;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblHorarios;
    private JButton btnSalir;

    private final HorarioSemanalController horarioController;
    private final SeccionController seccionController;
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] nombresDias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    public HorarioSemanalForm() {
        this(new HorarioSemanalController(), new SeccionController());
    }

    public HorarioSemanalForm(HorarioSemanalController horarioController, SeccionController seccionController) {
        this.horarioController = horarioController;
        this.seccionController = seccionController;
        configurarFormulario();
        cargarSecciones();
        configurarEventos();
        cargarHorarios();
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        btnActualizar.setEnabled(false);
        configurarSpinnerHora(spnHoraInicio, LocalTime.of(7, 0));
        configurarSpinnerHora(spnHoraFin, LocalTime.of(8, 0));
        cmbDia.removeAllItems();
        cmbDia.addItem(null);
        for (String dia : nombresDias) cmbDia.addItem(dia);
        ComboBoxRenderers.configurarDias(cmbDia);
        ComboBoxRenderers.configurarSecciones(cmbSeccion);
        configurarTabla();
    }

    private void configurarSpinnerHora(JSpinner spinner, LocalTime valorInicial) {
        spinner.setModel(new SpinnerDateModel(toDate(valorInicial), null, null, Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
    }

    private void configurarTabla() {
        tblHorarios.setModel(new DefaultTableModel(new Object[]{"ID", "Sección", "Día", "Hora inicio", "Hora fin"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblHorarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarSecciones() {
        try {
            cmbSeccion.removeAllItems();
            cmbSeccion.addItem(null);
            for (Seccion seccion : seccionController.listarActivos()) cmbSeccion.addItem(seccion);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());
        tblHorarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarHorario();
        });
    }

    private void guardar() {
        try {
            if (horarioController.guardar(construirHorario(0))) {
                JOptionPane.showMessageDialog(panelPrincipal, "Horario guardado correctamente.");
                limpiarFormulario();
                cargarHorarios();
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void actualizar() {
        try {
            int id = obtenerId();
            if (horarioController.actualizar(construirHorario(id))) {
                JOptionPane.showMessageDialog(panelPrincipal, "Horario actualizado correctamente.");
                cargarHorarios();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private HorarioSemanal construirHorario(int id) {
        Seccion seccion = (Seccion) cmbSeccion.getSelectedItem();
        if (seccion == null) throw new IllegalArgumentException("Seleccione una sección.");
        if (cmbDia.getSelectedIndex() <= 0) throw new IllegalArgumentException("Seleccione un día de la semana.");
        LocalTime inicio = leerHora(spnHoraInicio);
        LocalTime fin = leerHora(spnHoraFin);
        if (!fin.isAfter(inicio)) throw new IllegalArgumentException("La hora fin debe ser posterior a la hora inicio.");
        return new HorarioSemanal(id, seccion, DayOfWeek.of(cmbDia.getSelectedIndex()), inicio, fin);
    }

    private void cargarHorarios() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblHorarios.getModel();
            modelo.setRowCount(0);
            for (HorarioSemanal horario : horarioController.listar()) {
                modelo.addRow(new Object[]{horario.getIdHorario(), mostrarSeccion(horario.getSeccion()),
                        nombreDia(horario.getDiaSemana()), ComboBoxRenderers.hora(horario.getHoraInicio()),
                        ComboBoxRenderers.hora(horario.getHoraFin())});
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isBlank()) {
            cargarHorarios();
            return;
        }
        try {
            int id = Integer.parseInt(texto);
            horarioController.buscar(id).ifPresentOrElse(this::mostrarHorario,
                    () -> JOptionPane.showMessageDialog(panelPrincipal, "No se encontró el horario."));
        } catch (RuntimeException e) {
            mostrarError("Ingrese un ID de horario válido.");
        }
    }

    private void seleccionarHorario() {
        int fila = tblHorarios.getSelectedRow();
        if (fila < 0) return;
        try {
            horarioController.buscar(Integer.parseInt(tblHorarios.getValueAt(fila, 0).toString()))
                    .ifPresent(this::mostrarHorario);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarHorario(HorarioSemanal horario) {
        txtId.setText(String.valueOf(horario.getIdHorario()));
        seleccionarSeccion(horario.getSeccion());
        cmbDia.setSelectedIndex(horario.getDiaSemana().getValue());
        spnHoraInicio.setValue(toDate(horario.getHoraInicio()));
        spnHoraFin.setValue(toDate(horario.getHoraFin()));
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
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
    }

    private LocalTime leerHora(JSpinner spinner) {
        Date valor = (Date) spinner.getValue();
        return valor.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private Date toDate(LocalTime hora) {
        return Date.from(hora.atDate(java.time.LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }

    private String nombreDia(DayOfWeek dia) {
        return dia == null ? "" : nombresDias[dia.getValue() - 1];
    }

    private String mostrarSeccion(Seccion seccion) {
        return seccion == null ? "" : seccion.getCodigo() + " - " + (seccion.getCurso() == null ? "" : seccion.getCurso().getNombre());
    }

    private int obtenerId() {
        if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione un horario.");
        return Integer.parseInt(txtId.getText().trim());
    }

    private void seleccionarFilaPorId(int id) {
        for (int fila = 0; fila < tblHorarios.getRowCount(); fila++) {
            if (Integer.parseInt(tblHorarios.getValueAt(fila, 0).toString()) == id) {
                tblHorarios.setRowSelectionInterval(fila, fila);
                tblHorarios.scrollRectToVisible(tblHorarios.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void nuevo() {
        limpiarFormulario();
        spnHoraInicio.requestFocus();
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtBuscar.setText("");
        configurarSpinnerHora(spnHoraInicio, LocalTime.of(7, 0));
        configurarSpinnerHora(spnHoraFin, LocalTime.of(8, 0));
        if (cmbSeccion.getItemCount() > 0) cmbSeccion.setSelectedIndex(0);
        cmbDia.setSelectedIndex(0);
        tblHorarios.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(panelPrincipal,
                mensaje == null || mensaje.isBlank() ? "Ocurrió un error inesperado." : mensaje,
                "Horarios", JOptionPane.ERROR_MESSAGE);
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
