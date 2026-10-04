package universidad.asistencia.view;

import universidad.asistencia.controller.DispositivoController;
import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.model.Dispositivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Catálogo de dispositivos de marcaje. */
public class DispositivoForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JComboBox<MedioMarcaje> cmbTipo;
    private JTextField txtUbicacion;
    private JCheckBox chkActivo;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblDispositivos;
    private JButton btnSalir;

    private final DispositivoController dispositivoController;

    public DispositivoForm() { this(new DispositivoController()); }
    public DispositivoForm(DispositivoController dispositivoController) { this.dispositivoController = dispositivoController; configurarFormulario(); configurarEventos(); cargarDispositivos(); }
    private void configurarFormulario() { txtId.setEditable(false); chkActivo.setSelected(true); chkActivo.setEnabled(false); btnActualizar.setEnabled(false); btnDesactivar.setEnabled(false); cmbTipo.removeAllItems(); cmbTipo.addItem(null); for (MedioMarcaje tipo : MedioMarcaje.values()) cmbTipo.addItem(tipo); ComboBoxRenderers.configurarMedios(cmbTipo); configurarTabla(); }
    private void configurarTabla() { tblDispositivos.setModel(new DefaultTableModel(new Object[]{"ID", "Código", "Nombre", "Tipo", "Ubicación", "Activo"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } }); tblDispositivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); }
    private void configurarEventos() { btnNuevo.addActionListener(e -> nuevo()); btnGuardar.addActionListener(e -> guardar()); btnActualizar.addActionListener(e -> actualizar()); btnDesactivar.addActionListener(e -> cambiarEstado()); btnBuscar.addActionListener(e -> buscar()); btnSalir.addActionListener(e -> salir()); tblDispositivos.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) seleccionarDispositivo(); }); }
    private void guardar() { try { MedioMarcaje tipo = (MedioMarcaje) cmbTipo.getSelectedItem(); if (tipo == null) throw new IllegalArgumentException("Seleccione el tipo de dispositivo."); Dispositivo d = new Dispositivo(txtCodigo.getText().trim(), txtNombre.getText().trim(), tipo.name(), txtUbicacion.getText().trim()); if (dispositivoController.guardar(d)) { JOptionPane.showMessageDialog(panelPrincipal, "Dispositivo guardado correctamente."); limpiarFormulario(); cargarDispositivos(); } } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private void actualizar() { try { int id = obtenerId(); if (JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea actualizar este dispositivo?", "Confirmar actualización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return; if (dispositivoController.actualizar(construirDispositivo(id, chkActivo.isSelected()))) { JOptionPane.showMessageDialog(panelPrincipal, "Dispositivo actualizado correctamente."); cargarDispositivos(); seleccionarFilaPorId(id); } } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private void cambiarEstado() { try { int id = obtenerId(); boolean estado = !chkActivo.isSelected(); int respuesta = JOptionPane.showConfirmDialog(panelPrincipal, estado ? "¿Desea activar este dispositivo?" : "¿Desea desactivar este dispositivo?", "Confirmar cambio de estado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); if (respuesta != JOptionPane.YES_OPTION) return; if (dispositivoController.actualizar(construirDispositivo(id, estado))) { chkActivo.setSelected(estado); actualizarTextoEstado(estado); cargarDispositivos(); seleccionarFilaPorId(id); } } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private Dispositivo construirDispositivo(int id, boolean activo) { MedioMarcaje tipo = (MedioMarcaje) cmbTipo.getSelectedItem(); return new Dispositivo(id, txtCodigo.getText().trim(), txtNombre.getText().trim(), tipo == null ? null : tipo.name(), txtUbicacion.getText().trim(), activo); }
    private void cargarDispositivos() { try { DefaultTableModel modelo = (DefaultTableModel) tblDispositivos.getModel(); modelo.setRowCount(0); for (Dispositivo d : dispositivoController.listar()) modelo.addRow(new Object[]{d.getIdDispositivo(), d.getCodigo(), d.getNombre(), ComboBoxRenderers.medio(d.getTipo()), d.getUbicacion(), d.isActivo() ? "Sí" : "No"}); } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private void buscar() { String codigo = txtBuscar.getText().trim(); if (codigo.isBlank()) { cargarDispositivos(); return; } try { dispositivoController.buscarPorCodigo(codigo).ifPresentOrElse(this::mostrarDispositivo, () -> JOptionPane.showMessageDialog(panelPrincipal, "No se encontró el dispositivo.")); } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private void seleccionarDispositivo() { int fila = tblDispositivos.getSelectedRow(); if (fila < 0) return; try { dispositivoController.buscar(Integer.parseInt(tblDispositivos.getValueAt(fila, 0).toString())).ifPresent(this::mostrarDispositivo); } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private void mostrarDispositivo(Dispositivo d) { txtId.setText(String.valueOf(d.getIdDispositivo())); txtCodigo.setText(d.getCodigo()); txtNombre.setText(d.getNombre()); txtUbicacion.setText(d.getUbicacion()); try { cmbTipo.setSelectedItem(MedioMarcaje.valueOf(d.getTipo())); } catch (IllegalArgumentException e) { mostrarError("El tipo de dispositivo almacenado no es válido."); } chkActivo.setSelected(d.isActivo()); actualizarTextoEstado(d.isActivo()); btnGuardar.setEnabled(false); btnActualizar.setEnabled(true); btnDesactivar.setEnabled(true); seleccionarFilaPorId(d.getIdDispositivo()); }
    private void actualizarTextoEstado(boolean activo) { btnDesactivar.setText(activo ? "Desactivar" : "Activar"); }
    private int obtenerId() { if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione un dispositivo."); return Integer.parseInt(txtId.getText().trim()); }
    private void seleccionarFilaPorId(int id) { for (int f = 0; f < tblDispositivos.getRowCount(); f++) if (Integer.parseInt(tblDispositivos.getValueAt(f, 0).toString()) == id) { tblDispositivos.setRowSelectionInterval(f, f); tblDispositivos.scrollRectToVisible(tblDispositivos.getCellRect(f, 0, true)); return; } }
    private void nuevo() { limpiarFormulario(); txtCodigo.requestFocus(); }
    private void limpiarFormulario() { txtId.setText(""); txtCodigo.setText(""); txtNombre.setText(""); txtUbicacion.setText(""); txtBuscar.setText(""); chkActivo.setSelected(true); if (cmbTipo.getItemCount() > 0) cmbTipo.setSelectedIndex(0); tblDispositivos.clearSelection(); btnGuardar.setEnabled(true); btnActualizar.setEnabled(false); btnDesactivar.setEnabled(false); actualizarTextoEstado(true); }
    private void mostrarError(String mensaje) { if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado."; JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Error", JOptionPane.ERROR_MESSAGE); }
    private void salir() { int r = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); if (r == JOptionPane.YES_OPTION) { java.awt.Window v = SwingUtilities.getWindowAncestor(panelPrincipal); if (v != null) v.dispose(); } }
    public JPanel getPanelPrincipal() { return panelPrincipal; }
}
