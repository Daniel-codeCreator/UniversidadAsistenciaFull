package universidad.asistencia.view;

import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.model.Docente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

/** Catálogo de docentes. */
public class DocenteForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtCorreo;
    private JCheckBox chkActivo;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblDocentes;
    private JButton btnSalir;

    private final DocenteController docenteController;

    public DocenteForm() {
        this(new DocenteController());
    }

    public DocenteForm(DocenteController docenteController) {
        this.docenteController = docenteController;
        configurarFormulario();
        configurarEventos();
        cargarDocentes();
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        chkActivo.setSelected(true);
        chkActivo.setEnabled(false);
        btnActualizar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        configurarTabla();
    }

    private void configurarTabla() {
        tblDocentes.setModel(new DefaultTableModel(
                new Object[]{"ID", "Código", "Nombres", "Apellidos", "Correo", "Activo"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tblDocentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());
        tblDocentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarDocente();
        });
    }

    private void guardar() {
        try {
            Docente docente = new Docente(txtCodigo.getText().trim(), txtNombres.getText().trim(),
                    txtApellidos.getText().trim(), txtCorreo.getText().trim());
            if (docenteController.guardar(docente)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Docente guardado correctamente.");
                limpiarFormulario();
                cargarDocentes();
            }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cargarDocentes() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblDocentes.getModel();
            modelo.setRowCount(0);
            List<Docente> docentes = docenteController.listar();
            for (Docente d : docentes) modelo.addRow(new Object[]{d.getIdDocente(), d.getCodigoEmpleado(),
                    d.getNombres(), d.getApellidos(), d.getCorreo(), d.isActivo() ? "Sí" : "No"});
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void actualizar() {
        try {
            int id = obtenerId();
            if (!confirmar("¿Desea actualizar este docente?")) return;
            Docente docente = new Docente(id, txtCodigo.getText().trim(), txtNombres.getText().trim(),
                    txtApellidos.getText().trim(), txtCorreo.getText().trim(), chkActivo.isSelected());
            if (docenteController.actualizar(docente)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Docente actualizado correctamente.");
                cargarDocentes();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cambiarEstado() {
        try {
            int id = obtenerId();
            boolean nuevoEstado = !chkActivo.isSelected();
            if (!confirmar(nuevoEstado ? "¿Desea activar este docente?" : "¿Desea desactivar este docente?")) return;
            Docente docente = new Docente(id, txtCodigo.getText().trim(), txtNombres.getText().trim(),
                    txtApellidos.getText().trim(), txtCorreo.getText().trim(), nuevoEstado);
            if (docenteController.actualizar(docente)) {
                chkActivo.setSelected(nuevoEstado);
                actualizarTextoEstado(nuevoEstado);
                cargarDocentes();
                seleccionarFilaPorId(id);
            }
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void buscar() {
        String codigo = txtBuscar.getText().trim();
        if (codigo.isBlank()) { cargarDocentes(); return; }
        try {
            Optional<Docente> resultado = docenteController.buscarPorCodigo(codigo);
            if (resultado.isPresent()) {
                mostrarDocente(resultado.get());
                seleccionarFilaPorId(resultado.get().getIdDocente());
            } else JOptionPane.showMessageDialog(panelPrincipal, "No se encontró el docente.");
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void seleccionarDocente() {
        int fila = tblDocentes.getSelectedRow();
        if (fila < 0) return;
        try {
            int id = Integer.parseInt(tblDocentes.getValueAt(fila, 0).toString());
            docenteController.buscar(id).ifPresent(this::mostrarDocente);
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void mostrarDocente(Docente d) {
        txtId.setText(String.valueOf(d.getIdDocente()));
        txtCodigo.setText(d.getCodigoEmpleado());
        txtNombres.setText(d.getNombres());
        txtApellidos.setText(d.getApellidos());
        txtCorreo.setText(d.getCorreo());
        chkActivo.setSelected(d.isActivo());
        actualizarTextoEstado(d.isActivo());
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnDesactivar.setEnabled(true);
    }

    private void actualizarTextoEstado(boolean activo) { btnDesactivar.setText(activo ? "Desactivar" : "Activar"); }

    private void seleccionarFilaPorId(int id) {
        for (int fila = 0; fila < tblDocentes.getRowCount(); fila++) {
            if (Integer.parseInt(tblDocentes.getValueAt(fila, 0).toString()) == id) {
                tblDocentes.setRowSelectionInterval(fila, fila);
                tblDocentes.scrollRectToVisible(tblDocentes.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private int obtenerId() {
        if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione un docente.");
        return Integer.parseInt(txtId.getText().trim());
    }

    private void nuevo() { limpiarFormulario(); txtCodigo.requestFocus(); }

    private void limpiarFormulario() {
        txtId.setText(""); txtCodigo.setText(""); txtNombres.setText(""); txtApellidos.setText("");
        txtCorreo.setText(""); txtBuscar.setText(""); chkActivo.setSelected(true);
        tblDocentes.clearSelection(); btnGuardar.setEnabled(true); btnActualizar.setEnabled(false);
        btnDesactivar.setEnabled(false); actualizarTextoEstado(true);
    }

    private void mostrarError(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado.";
        JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(panelPrincipal, mensaje, "Confirmar operación",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea regresar al menú principal?",
                "Regresar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            java.awt.Window ventana = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (ventana != null) ventana.dispose();
        }
    }

    public JPanel getPanelPrincipal() { return panelPrincipal; }
}
