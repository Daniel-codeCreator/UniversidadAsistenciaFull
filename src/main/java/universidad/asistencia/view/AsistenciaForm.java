package universidad.asistencia.view;

import universidad.asistencia.controller.AsistenciaController;
import universidad.asistencia.controller.DispositivoController;
import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.controller.SesionClaseController;
import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.enums.ResultadoAsistencia;
import universidad.asistencia.enums.TipoMarcaje;
import universidad.asistencia.model.Dispositivo;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.Marcaje;
import universidad.asistencia.model.SesionClase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Pantalla para registrar y consultar marcajes. */
public class AsistenciaForm {

    private JPanel panelPrincipal;
    private JComboBox<Estudiante> cmbEstudiante;
    private JComboBox<SesionClase> cmbSesion;
    private JComboBox<Dispositivo> cmbDispositivo;
    private JComboBox<TipoMarcaje> cmbTipo;
    private JComboBox<MedioMarcaje> cmbMedio;
    private JButton btnRegistrar;
    private JButton btnResultado;
    private JButton btnHistorial;
    private JButton btnAlertas;
    private JTextField txtPorcentaje;
    private JLabel lblResultado;
    private JTable tblHistorial;
    private JTable tblAlertas;
    private JButton btnSalir;

    private final AsistenciaController asistenciaController;
    private final EstudianteController estudianteController;
    private final SesionClaseController sesionController;
    private final DispositivoController dispositivoController;

    public AsistenciaForm() { this(new AsistenciaController(), new EstudianteController(), new SesionClaseController(), new DispositivoController()); }
    public AsistenciaForm(AsistenciaController asistenciaController, EstudianteController estudianteController, SesionClaseController sesionController, DispositivoController dispositivoController) {
        this.asistenciaController = asistenciaController; this.estudianteController = estudianteController; this.sesionController = sesionController; this.dispositivoController = dispositivoController;
        configurarFormulario(); cargarCombos(); configurarEventos();
    }

    private void configurarFormulario() {
        txtPorcentaje.setEditable(false); cmbTipo.removeAllItems(); cmbTipo.addItem(null); for (TipoMarcaje t : TipoMarcaje.values()) cmbTipo.addItem(t);
        cmbMedio.removeAllItems(); cmbMedio.addItem(null); for (MedioMarcaje m : MedioMarcaje.values()) cmbMedio.addItem(m);
        ComboBoxRenderers.configurarTiposMarcaje(cmbTipo); ComboBoxRenderers.configurarMedios(cmbMedio);
        ComboBoxRenderers.configurarEstudiantes(cmbEstudiante); ComboBoxRenderers.configurarSesiones(cmbSesion); ComboBoxRenderers.configurarDispositivos(cmbDispositivo);
        configurarTablas();
    }
    private void configurarTablas() {
        tblHistorial.setModel(new DefaultTableModel(new Object[]{"ID", "Estudiante", "Sesión", "Fecha y hora", "Tipo", "Medio", "Dispositivo"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } });
        tblAlertas.setModel(new DefaultTableModel(new Object[]{"Carnet", "Nombres", "Apellidos", "% asistencia"}, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } });
        tblHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tblAlertas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    private void cargarCombos() { try { cmbEstudiante.removeAllItems(); cmbEstudiante.addItem(null); for (Estudiante e : estudianteController.listarActivos()) cmbEstudiante.addItem(e); cmbSesion.removeAllItems(); cmbSesion.addItem(null); for (SesionClase s : sesionController.listar()) cmbSesion.addItem(s); cmbDispositivo.removeAllItems(); cmbDispositivo.addItem(null); for (Dispositivo d : dispositivoController.listarActivos()) cmbDispositivo.addItem(d); } catch (RuntimeException e) { mostrarError(e.getMessage()); } }
    private void configurarEventos() { btnRegistrar.addActionListener(e -> registrar()); btnResultado.addActionListener(e -> consultarResultado()); btnHistorial.addActionListener(e -> cargarHistorial()); btnAlertas.addActionListener(e -> cargarAlertas()); btnSalir.addActionListener(e -> salir()); }

    private void registrar() {
        try {
            Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem(); SesionClase sesion = (SesionClase) cmbSesion.getSelectedItem(); Dispositivo dispositivo = (Dispositivo) cmbDispositivo.getSelectedItem();
            if (estudiante == null || sesion == null || dispositivo == null) throw new IllegalArgumentException("Seleccione estudiante, sesión y dispositivo.");
            TipoMarcaje tipo = (TipoMarcaje) cmbTipo.getSelectedItem();
            MedioMarcaje medio = (MedioMarcaje) cmbMedio.getSelectedItem();
            if (tipo == null || medio == null) throw new IllegalArgumentException("Seleccione tipo y medio de marcaje.");
            Marcaje marcaje = asistenciaController.registrarMarcaje(estudiante.getIdEstudiante(), sesion.getIdSesion(), dispositivo.getIdDispositivo(), tipo, medio);
            JOptionPane.showMessageDialog(panelPrincipal, "Marcaje registrado: " + ComboBoxRenderers.texto(marcaje.getTipo())); cargarHistorial(); consultarResultado();
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void consultarResultado() {
        try { Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem(); SesionClase sesion = (SesionClase) cmbSesion.getSelectedItem(); if (estudiante == null || sesion == null) throw new IllegalArgumentException("Seleccione estudiante y sesión."); ResultadoAsistencia resultado = asistenciaController.calcularResultadoAsistencia(estudiante.getIdEstudiante(), sesion.getIdSesion()); lblResultado.setText("Resultado: " + ComboBoxRenderers.texto(resultado)); txtPorcentaje.setText(String.format("%.2f", asistenciaController.calcularPorcentajeAsistencia(estudiante.getIdEstudiante()))); }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cargarHistorial() {
        try { Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem(); if (estudiante == null) throw new IllegalArgumentException("Seleccione un estudiante."); DefaultTableModel modelo = (DefaultTableModel) tblHistorial.getModel(); modelo.setRowCount(0); for (Marcaje m : asistenciaController.obtenerHistorial(estudiante.getIdEstudiante())) modelo.addRow(new Object[]{m.getIdMarcaje(), mostrarEstudiante(m.getEstudiante()), mostrarSesion(m.getSesionClase()), ComboBoxRenderers.fechaHora(m.getFechaHora()), ComboBoxRenderers.texto(m.getTipo()), ComboBoxRenderers.texto(m.getMedio()), m.getDispositivo() == null ? "" : m.getDispositivo().getCodigo()}); }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void cargarAlertas() {
        try { DefaultTableModel modelo = (DefaultTableModel) tblAlertas.getModel(); modelo.setRowCount(0); for (Estudiante e : asistenciaController.obtenerEstudiantesBajo80()) modelo.addRow(new Object[]{e.getCarnet(), e.getNombres(), e.getApellidos(), String.format("%.2f", asistenciaController.calcularPorcentajeAsistencia(e.getIdEstudiante()))}); }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private String mostrarEstudiante(Estudiante e) { return e == null ? "" : e.getCarnet() + " - " + e.getNombres() + " " + e.getApellidos(); }
    private String mostrarSesion(SesionClase s) { return s == null ? "" : ComboBoxRenderers.fecha(s.getFecha()) + " / " + (s.getSeccion() == null ? "" : s.getSeccion().getCodigo()); }
    private void mostrarError(String mensaje) { if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado."; JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Asistencia", JOptionPane.ERROR_MESSAGE); }
    private void salir() { int r = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); if (r == JOptionPane.YES_OPTION) { java.awt.Window v = SwingUtilities.getWindowAncestor(panelPrincipal); if (v != null) v.dispose(); } }
    public JPanel getPanelPrincipal() { return panelPrincipal; }
}
