package universidad.asistencia.view;

import universidad.asistencia.controller.CursoController;
import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.controller.PeriodoAcademicoController;
import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.model.Curso;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.model.Seccion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

/** Catálogo de secciones y sus relaciones académicas. */
public class SeccionForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JTextField txtCodigo;
    private JComboBox<Curso> cmbCurso;
    private JComboBox<PeriodoAcademico> cmbPeriodo;
    private JComboBox<Docente> cmbDocente;
    private JTextField txtAula;
    private JCheckBox chkActivo;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblSecciones;
    private JButton btnSalir;

    private final SeccionController seccionController;
    private final CursoController cursoController;
    private final PeriodoAcademicoController periodoController;
    private final DocenteController docenteController;

    public SeccionForm() {
        this(new SeccionController(), new CursoController(), new PeriodoAcademicoController(), new DocenteController());
    }

    public SeccionForm(SeccionController seccionController, CursoController cursoController,
                       PeriodoAcademicoController periodoController, DocenteController docenteController) {
        this.seccionController = seccionController; this.cursoController = cursoController;
        this.periodoController = periodoController; this.docenteController = docenteController;
        configurarFormulario(); cargarCombos(); configurarEventos(); cargarSecciones();
    }

    private void configurarFormulario() { txtId.setEditable(false); chkActivo.setSelected(true); chkActivo.setEnabled(false); btnActualizar.setEnabled(false); btnDesactivar.setEnabled(false); ComboBoxRenderers.configurar(cmbCurso); ComboBoxRenderers.configurarPeriodos(cmbPeriodo); ComboBoxRenderers.configurarDocentes(cmbDocente); configurarTabla(); }

    private void configurarTabla() {
        tblSecciones.setModel(new DefaultTableModel(new Object[]{"ID", "Código", "Curso", "Periodo", "Docente", "Aula", "Activo"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tblSecciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarCombos() {
        try {
            cmbCurso.removeAllItems(); cmbCurso.addItem(null); for (Curso c : cursoController.listarActivos()) cmbCurso.addItem(c);
            cmbPeriodo.removeAllItems(); cmbPeriodo.addItem(null); for (PeriodoAcademico p : periodoController.listarActivos()) cmbPeriodo.addItem(p);
            cmbDocente.removeAllItems(); cmbDocente.addItem(null); for (Docente d : docenteController.listarActivos()) cmbDocente.addItem(d);
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo()); btnGuardar.addActionListener(e -> guardar()); btnActualizar.addActionListener(e -> actualizar());
        btnDesactivar.addActionListener(e -> cambiarEstado()); btnBuscar.addActionListener(e -> buscar()); btnSalir.addActionListener(e -> salir());
        tblSecciones.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) seleccionarSeccion(); });
    }

    private void guardar() {
        try {
            Seccion seccion = construirSeccion(0, true);
            if (seccionController.guardar(seccion)) { JOptionPane.showMessageDialog(panelPrincipal, "Sección guardada correctamente."); limpiarFormulario(); cargarSecciones(); }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void actualizar() {
        try {
            int id = obtenerId(); if (JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea actualizar esta sección?", "Confirmar actualización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return; Seccion s = construirSeccion(id, chkActivo.isSelected());
            if (seccionController.actualizar(s)) { JOptionPane.showMessageDialog(panelPrincipal, "Sección actualizada correctamente."); cargarSecciones(); seleccionarFilaPorId(id); }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cambiarEstado() {
        try {
            int id = obtenerId(); boolean estado = !chkActivo.isSelected();
            int respuesta = JOptionPane.showConfirmDialog(panelPrincipal, estado ? "¿Desea activar esta sección?" : "¿Desea desactivar esta sección?", "Confirmar cambio de estado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) return;
            if (seccionController.actualizar(construirSeccion(id, estado))) { chkActivo.setSelected(estado); actualizarTextoEstado(estado); cargarSecciones(); seleccionarFilaPorId(id); }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private Seccion construirSeccion(int id, boolean activo) { Curso curso = (Curso) cmbCurso.getSelectedItem(); PeriodoAcademico periodo = (PeriodoAcademico) cmbPeriodo.getSelectedItem(); Docente docente = (Docente) cmbDocente.getSelectedItem(); if (curso == null || periodo == null || docente == null) throw new IllegalArgumentException("Seleccione curso, periodo y docente."); if (txtCodigo.getText().trim().isBlank() || txtAula.getText().trim().isBlank()) throw new IllegalArgumentException("El código y el aula son obligatorios."); return new Seccion(id, txtCodigo.getText().trim(), curso, periodo, docente, txtAula.getText().trim(), activo); }

    private void cargarSecciones() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblSecciones.getModel(); modelo.setRowCount(0);
            for (Seccion s : seccionController.listar()) modelo.addRow(new Object[]{s.getIdSeccion(), s.getCodigo(), nombreCurso(s), nombrePeriodo(s), nombreDocente(s), s.getAulaAsignada(), s.isActivo() ? "Sí" : "No"});
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void buscar() {
        String codigo = txtBuscar.getText().trim(); if (codigo.isBlank()) { cargarSecciones(); return; }
        try { Optional<Seccion> r = seccionController.buscarPorCodigo(codigo); if (r.isPresent()) { mostrarSeccion(r.get()); seleccionarFilaPorId(r.get().getIdSeccion()); } else JOptionPane.showMessageDialog(panelPrincipal, "No se encontró la sección."); }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void seleccionarSeccion() {
        int fila = tblSecciones.getSelectedRow(); if (fila < 0) return;
        try { seccionController.buscar(Integer.parseInt(tblSecciones.getValueAt(fila, 0).toString())).ifPresent(this::mostrarSeccion); }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void mostrarSeccion(Seccion s) {
        txtId.setText(String.valueOf(s.getIdSeccion())); txtCodigo.setText(s.getCodigo()); txtAula.setText(s.getAulaAsignada());
        seleccionarPorId(cmbCurso, s.getCurso() == null ? 0 : s.getCurso().getIdCurso(), true);
        seleccionarPorId(cmbPeriodo, s.getPeriodoAcademico() == null ? 0 : s.getPeriodoAcademico().getIdPeriodo(), true);
        seleccionarPorId(cmbDocente, s.getDocente() == null ? 0 : s.getDocente().getIdDocente(), true);
        chkActivo.setSelected(s.isActivo()); actualizarTextoEstado(s.isActivo()); btnGuardar.setEnabled(false); btnActualizar.setEnabled(true); btnDesactivar.setEnabled(true);
    }

    private void seleccionarPorId(JComboBox<?> combo, int id, boolean usarId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i); if (item == null) continue; int itemId = item instanceof Curso c ? c.getIdCurso() : item instanceof PeriodoAcademico p ? p.getIdPeriodo() : ((Docente) item).getIdDocente();
            if (itemId == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private String nombreCurso(Seccion s) { return s.getCurso() == null ? "" : s.getCurso().getCodigo() + " - " + s.getCurso().getNombre(); }
    private String nombrePeriodo(Seccion s) { return s.getPeriodoAcademico() == null ? "" : s.getPeriodoAcademico().getNombre(); }
    private String nombreDocente(Seccion s) { return s.getDocente() == null ? "" : s.getDocente().getCodigoEmpleado() + " - " + s.getDocente().getNombres() + " " + s.getDocente().getApellidos(); }
    private void actualizarTextoEstado(boolean activo) { btnDesactivar.setText(activo ? "Desactivar" : "Activar"); }
    private int obtenerId() { if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione una sección."); return Integer.parseInt(txtId.getText().trim()); }
    private void seleccionarFilaPorId(int id) { for (int f = 0; f < tblSecciones.getRowCount(); f++) if (Integer.parseInt(tblSecciones.getValueAt(f, 0).toString()) == id) { tblSecciones.setRowSelectionInterval(f, f); tblSecciones.scrollRectToVisible(tblSecciones.getCellRect(f, 0, true)); return; } }
    private void nuevo() { limpiarFormulario(); txtCodigo.requestFocus(); }
    private void limpiarFormulario() { txtId.setText(""); txtCodigo.setText(""); txtAula.setText(""); txtBuscar.setText(""); chkActivo.setSelected(true); if (cmbCurso.getItemCount() > 0) cmbCurso.setSelectedIndex(0); if (cmbPeriodo.getItemCount() > 0) cmbPeriodo.setSelectedIndex(0); if (cmbDocente.getItemCount() > 0) cmbDocente.setSelectedIndex(0); tblSecciones.clearSelection(); btnGuardar.setEnabled(true); btnActualizar.setEnabled(false); btnDesactivar.setEnabled(false); actualizarTextoEstado(true); }
    private void mostrarError(String mensaje) { if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado."; JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Error", JOptionPane.ERROR_MESSAGE); }
    private void salir() { int r = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); if (r == JOptionPane.YES_OPTION) { java.awt.Window v = SwingUtilities.getWindowAncestor(panelPrincipal); if (v != null) v.dispose(); } }
    public JPanel getPanelPrincipal() { return panelPrincipal; }
}
