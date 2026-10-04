package universidad.asistencia.view;

import universidad.asistencia.controller.PeriodoAcademicoController;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.util.DateUtils;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Catálogo de periodos académicos. */
public class PeriodoAcademicoForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JTextField txtNombre;
    private JDateChooser dcFechaInicio;
    private JDateChooser dcFechaFin;
    private JCheckBox chkActivo;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblPeriodos;
    private JButton btnSalir;

    private final PeriodoAcademicoController periodoController;

    public PeriodoAcademicoForm() { this(new PeriodoAcademicoController()); }

    public PeriodoAcademicoForm(PeriodoAcademicoController periodoController) {
        this.periodoController = periodoController;
        configurarFormulario(); configurarEventos(); cargarPeriodos();
    }

    private void configurarFormulario() {
        txtId.setEditable(false); chkActivo.setSelected(true); chkActivo.setEnabled(false);
        dcFechaInicio.setDateFormatString("dd/MM/yyyy"); dcFechaFin.setDateFormatString("dd/MM/yyyy");
        btnActualizar.setEnabled(false); btnDesactivar.setEnabled(false); configurarTabla();
    }

    private void configurarTabla() {
        tblPeriodos.setModel(new DefaultTableModel(new Object[]{"ID", "Nombre", "Fecha inicio", "Fecha fin", "Activo"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tblPeriodos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo()); btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar()); btnDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar()); btnSalir.addActionListener(e -> salir());
        tblPeriodos.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) seleccionarPeriodo(); });
    }

    private void guardar() {
        try {
            PeriodoAcademico periodo = new PeriodoAcademico(txtNombre.getText().trim(), leerFecha(dcFechaInicio), leerFecha(dcFechaFin));
            if (periodoController.guardar(periodo)) { JOptionPane.showMessageDialog(panelPrincipal, "Periodo guardado correctamente."); limpiarFormulario(); cargarPeriodos(); }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void actualizar() {
        try {
            int id = obtenerId();
            if (!confirmar("¿Desea actualizar este periodo?")) return;
            PeriodoAcademico periodo = new PeriodoAcademico(id, txtNombre.getText().trim(), leerFecha(dcFechaInicio), leerFecha(dcFechaFin), chkActivo.isSelected());
            if (periodoController.actualizar(periodo)) { JOptionPane.showMessageDialog(panelPrincipal, "Periodo actualizado correctamente."); cargarPeriodos(); seleccionarFilaPorId(id); }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cambiarEstado() {
        try {
            int id = obtenerId(); boolean nuevoEstado = !chkActivo.isSelected();
            if (!confirmarEstado(nuevoEstado)) return;
            PeriodoAcademico periodo = new PeriodoAcademico(id, txtNombre.getText().trim(), leerFecha(dcFechaInicio), leerFecha(dcFechaFin), nuevoEstado);
            if (periodoController.actualizar(periodo)) { chkActivo.setSelected(nuevoEstado); actualizarTextoEstado(nuevoEstado); cargarPeriodos(); seleccionarFilaPorId(id); }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cargarPeriodos() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblPeriodos.getModel(); modelo.setRowCount(0);
            List<PeriodoAcademico> periodos = periodoController.listar();
            for (PeriodoAcademico p : periodos) modelo.addRow(new Object[]{p.getIdPeriodo(), p.getNombre(), ComboBoxRenderers.fecha(p.getFechaInicio()), ComboBoxRenderers.fecha(p.getFechaFin()), p.isActivo() ? "Sí" : "No"});
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void buscar() {
        String nombre = txtBuscar.getText().trim(); if (nombre.isBlank()) { cargarPeriodos(); return; }
        try {
            Optional<PeriodoAcademico> resultado = periodoController.buscarPorNombre(nombre);
            if (resultado.isPresent()) { mostrarPeriodo(resultado.get()); seleccionarFilaPorId(resultado.get().getIdPeriodo()); }
            else JOptionPane.showMessageDialog(panelPrincipal, "No se encontró el periodo.");
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void seleccionarPeriodo() {
        int fila = tblPeriodos.getSelectedRow(); if (fila < 0) return;
        try { periodoController.buscar(Integer.parseInt(tblPeriodos.getValueAt(fila, 0).toString())).ifPresent(this::mostrarPeriodo); }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void mostrarPeriodo(PeriodoAcademico p) {
        txtId.setText(String.valueOf(p.getIdPeriodo())); txtNombre.setText(p.getNombre());
        dcFechaInicio.setDate(DateUtils.toDate(p.getFechaInicio())); dcFechaFin.setDate(DateUtils.toDate(p.getFechaFin()));
        chkActivo.setSelected(p.isActivo()); actualizarTextoEstado(p.isActivo()); btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true); btnDesactivar.setEnabled(true);
    }

    private LocalDate leerFecha(JDateChooser campo) {
        if (campo.getDate() == null) throw new IllegalArgumentException("Seleccione una fecha válida.");
        return DateUtils.toLocalDate(campo.getDate());
    }

    private void actualizarTextoEstado(boolean activo) { btnDesactivar.setText(activo ? "Desactivar" : "Activar"); }
    private int obtenerId() { if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione un periodo."); return Integer.parseInt(txtId.getText().trim()); }
    private void seleccionarFilaPorId(int id) { for (int f = 0; f < tblPeriodos.getRowCount(); f++) if (Integer.parseInt(tblPeriodos.getValueAt(f, 0).toString()) == id) { tblPeriodos.setRowSelectionInterval(f, f); tblPeriodos.scrollRectToVisible(tblPeriodos.getCellRect(f, 0, true)); return; } }
    private void nuevo() { limpiarFormulario(); txtNombre.requestFocus(); }
    private void limpiarFormulario() { txtId.setText(""); txtNombre.setText(""); dcFechaInicio.setDate(null); dcFechaFin.setDate(null); txtBuscar.setText(""); chkActivo.setSelected(true); tblPeriodos.clearSelection(); btnGuardar.setEnabled(true); btnActualizar.setEnabled(false); btnDesactivar.setEnabled(false); actualizarTextoEstado(true); }
    private boolean confirmarEstado(boolean nuevoEstado) { int respuesta = JOptionPane.showConfirmDialog(panelPrincipal, nuevoEstado ? "¿Desea activar este periodo?" : "¿Desea desactivar este periodo?", "Confirmar cambio de estado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); return respuesta == JOptionPane.YES_OPTION; }
    private boolean confirmar(String mensaje) { return JOptionPane.showConfirmDialog(panelPrincipal, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION; }
    private void createUIComponents() {
        dcFechaInicio = new JDateChooser();
        dcFechaFin = new JDateChooser();
    }
    private void mostrarError(String mensaje) { if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado."; JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Error", JOptionPane.ERROR_MESSAGE); }
    private void salir() { int r = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); if (r == JOptionPane.YES_OPTION) { java.awt.Window v = SwingUtilities.getWindowAncestor(panelPrincipal); if (v != null) v.dispose(); } }
    public JPanel getPanelPrincipal() { return panelPrincipal; }
}
