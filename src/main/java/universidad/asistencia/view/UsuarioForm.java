package universidad.asistencia.view;

import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.controller.UsuarioController;
import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.integration.facial.FacialAuthClient;
import universidad.asistencia.integration.facial.FacialClientException;
import universidad.asistencia.integration.facial.FacialEnrollmentResult;
import universidad.asistencia.integration.facial.FacialStatus;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Usuario;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import java.util.concurrent.ExecutionException;

/** Administración de usuarios sin exponer el hash de contraseña. */
public class UsuarioForm {

    private JPanel panelPrincipal;
    private JTextField txtId;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<RolUsuario> cmbRol;
    private JComboBox<Docente> cmbDocente;
    private JCheckBox chkActivo;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblUsuarios;
    private JButton btnSalir;
    private JButton btnRegistrarRostro;
    private JButton btnEliminarRostro;
    private JLabel lblEstadoRostro;

    private final UsuarioController usuarioController;
    private final DocenteController docenteController;
    private final FacialAuthClient facialAuthClient;
    private final Usuario usuarioAutenticado;
    private Usuario usuarioSeleccionado;
    private boolean perfilFacialRegistrado;

    public UsuarioForm() {
        this(null);
    }

    public UsuarioForm(Usuario usuarioAutenticado) {
        this(new UsuarioController(), new DocenteController(), new FacialAuthClient(), usuarioAutenticado);
    }

    public UsuarioForm(UsuarioController usuarioController, DocenteController docenteController) {
        this(usuarioController, docenteController, new FacialAuthClient(), null);
    }

    public UsuarioForm(UsuarioController usuarioController, DocenteController docenteController,
                       Usuario usuarioAutenticado) {
        this(usuarioController, docenteController, new FacialAuthClient(), usuarioAutenticado);
    }

    public UsuarioForm(UsuarioController usuarioController, DocenteController docenteController,
                       FacialAuthClient facialAuthClient, Usuario usuarioAutenticado) {
        this.usuarioController = usuarioController;
        this.docenteController = docenteController;
        this.facialAuthClient = facialAuthClient;
        this.usuarioAutenticado = usuarioAutenticado;
        configurarFormulario();
        cargarDocentes();
        configurarEventos();
        cargarUsuarios();
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        chkActivo.setSelected(true);
        chkActivo.setEnabled(false);
        cmbRol.removeAllItems();
        cmbRol.addItem(null);
        for (RolUsuario rol : RolUsuario.values()) cmbRol.addItem(rol);
        ComboBoxRenderers.configurarRoles(cmbRol);
        ComboBoxRenderers.configurarDocentes(cmbDocente);
        btnActualizar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        btnRegistrarRostro.setEnabled(false);
        btnEliminarRostro.setEnabled(false);
        lblEstadoRostro.setText("Seleccione un usuario.");
        actualizarTextoEstado(true);
        configurarTabla();
        actualizarEstadoDocente();
    }

    private void configurarTabla() {
        tblUsuarios.setModel(new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Rol", "Docente", "Activo", "Fecha creación"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarDocentes() {
        try {
            cmbDocente.removeAllItems();
            cmbDocente.addItem(null);
            for (Docente docente : docenteController.listarActivos()) cmbDocente.addItem(docente);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void configurarEventos() {
        cmbRol.addActionListener(e -> actualizarEstadoDocente());
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnDesactivar.addActionListener(e -> cambiarEstado());
        btnRegistrarRostro.addActionListener(e -> registrarRostro());
        btnEliminarRostro.addActionListener(e -> eliminarRostro());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());
        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarUsuario();
        });
    }

    private void actualizarEstadoDocente() {
        boolean esDocente = cmbRol.getSelectedItem() == RolUsuario.DOCENTE;
        cmbDocente.setEnabled(esDocente);
        if (!esDocente) cmbDocente.setSelectedItem(null);
    }

    private void guardar() {
        try {
            RolUsuario rol = (RolUsuario) cmbRol.getSelectedItem();
            if (rol == null) throw new IllegalArgumentException("Seleccione el rol del usuario.");
            Docente docente = rol == RolUsuario.DOCENTE ? (Docente) cmbDocente.getSelectedItem() : null;
            if (rol == RolUsuario.DOCENTE && docente == null) {
                throw new IllegalArgumentException("Seleccione el docente asociado.");
            }
            Usuario usuario = usuarioController.crearUsuario(
                    txtUsuario.getText().trim(), new String(txtPassword.getPassword()), rol, docente);
            JOptionPane.showMessageDialog(panelPrincipal, "Usuario creado correctamente: " + usuario.getUsuario());
            limpiarFormulario();
            cargarUsuarios();
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void actualizar() {
        try {
            if (usuarioSeleccionado == null) throw new IllegalArgumentException("Seleccione un usuario.");
            if (!confirmar("¿Desea actualizar este usuario?", "Confirmar actualización")) return;
            Usuario usuario = construirUsuario(chkActivo.isSelected());
            String password = new String(txtPassword.getPassword());
            boolean actualizado = password.isBlank()
                    ? usuarioController.actualizar(usuario)
                    : usuarioController.actualizarConPassword(usuario, password);
            if (actualizado) {
                JOptionPane.showMessageDialog(panelPrincipal, "Usuario actualizado correctamente.");
                cargarUsuarios();
                seleccionarFilaPorId(usuario.getIdUsuario());
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cambiarEstado() {
        try {
            if (usuarioSeleccionado == null) throw new IllegalArgumentException("Seleccione un usuario.");
            if (esUsuarioAutenticadoActivo(usuarioSeleccionado)) {
                throw new IllegalArgumentException("No puede desactivar el usuario con el que inició sesión.");
            }
            boolean nuevoEstado = !chkActivo.isSelected();
            String accion = nuevoEstado ? "activar" : "desactivar";
            if (!confirmar("¿Desea " + accion + " este usuario?", "Confirmar cambio de estado")) return;
            if (usuarioController.actualizar(construirUsuario(nuevoEstado))) {
                chkActivo.setSelected(nuevoEstado);
                actualizarTextoEstado(nuevoEstado);
                JOptionPane.showMessageDialog(panelPrincipal,
                        nuevoEstado ? "Usuario activado correctamente." : "Usuario desactivado correctamente.");
                cargarUsuarios();
                seleccionarFilaPorId(usuarioSeleccionado.getIdUsuario());
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private Usuario construirUsuario(boolean activo) {
        RolUsuario rol = (RolUsuario) cmbRol.getSelectedItem();
        if (rol == null) throw new IllegalArgumentException("Seleccione el rol del usuario.");
        Docente docente = rol == RolUsuario.DOCENTE ? (Docente) cmbDocente.getSelectedItem() : null;
        if (rol == RolUsuario.DOCENTE && docente == null) {
            throw new IllegalArgumentException("Seleccione el docente asociado.");
        }
        return new Usuario(obtenerId(), txtUsuario.getText().trim(), usuarioSeleccionado.getPasswordHash(),
                rol, docente, activo, usuarioSeleccionado.getFechaCreacion());
    }

    private void cargarUsuarios() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblUsuarios.getModel();
            modelo.setRowCount(0);
            for (Usuario usuario : usuarioController.listar()) {
                modelo.addRow(new Object[]{usuario.getIdUsuario(), usuario.getUsuario(),
                        ComboBoxRenderers.texto(usuario.getRol()),
                        usuario.getDocente() == null ? "" : usuario.getDocente().getCodigoEmpleado(),
                        usuario.isActivo() ? "Sí" : "No",
                        ComboBoxRenderers.fechaHora(usuario.getFechaCreacion())});
            }
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String nombre = txtBuscar.getText().trim();
        if (nombre.isBlank()) {
            cargarUsuarios();
            return;
        }
        try {
            usuarioController.buscarPorUsuario(nombre).ifPresentOrElse(this::mostrarUsuario,
                    () -> JOptionPane.showMessageDialog(panelPrincipal, "No se encontró el usuario."));
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void seleccionarUsuario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila < 0) return;
        try {
            usuarioController.buscar(Integer.parseInt(tblUsuarios.getValueAt(fila, 0).toString()))
                    .ifPresent(this::mostrarUsuario);
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarUsuario(Usuario usuario) {
        usuarioSeleccionado = usuario;
        txtId.setText(String.valueOf(usuario.getIdUsuario()));
        txtUsuario.setText(usuario.getUsuario());
        txtPassword.setText("");
        cmbRol.setSelectedItem(usuario.getRol());
        seleccionarDocente(usuario.getDocente());
        chkActivo.setSelected(usuario.isActivo());
        actualizarTextoEstado(usuario.isActivo());
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnDesactivar.setEnabled(!esUsuarioAutenticadoActivo(usuario));
        consultarEstadoRostro(usuario.getIdUsuario());
    }

    private void seleccionarDocente(Docente buscado) {
        if (buscado == null) {
            cmbDocente.setSelectedItem(null);
            return;
        }
        for (int i = 0; i < cmbDocente.getItemCount(); i++) {
            Docente actual = cmbDocente.getItemAt(i);
            if (actual != null && actual.getIdDocente() == buscado.getIdDocente()) {
                cmbDocente.setSelectedIndex(i);
                return;
            }
        }
    }

    private int obtenerId() {
        if (txtId.getText().isBlank()) throw new IllegalArgumentException("Seleccione un usuario.");
        return Integer.parseInt(txtId.getText().trim());
    }

    private void seleccionarFilaPorId(int id) {
        for (int fila = 0; fila < tblUsuarios.getRowCount(); fila++) {
            if (Integer.parseInt(tblUsuarios.getValueAt(fila, 0).toString()) == id) {
                tblUsuarios.setRowSelectionInterval(fila, fila);
                tblUsuarios.scrollRectToVisible(tblUsuarios.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void nuevo() {
        limpiarFormulario();
        txtUsuario.requestFocus();
    }

    private void limpiarFormulario() {
        usuarioSeleccionado = null;
        perfilFacialRegistrado = false;
        txtId.setText("");
        txtUsuario.setText("");
        txtPassword.setText("");
        chkActivo.setSelected(true);
        txtBuscar.setText("");
        cmbRol.setSelectedItem(RolUsuario.ADMIN);
        actualizarEstadoDocente();
        tblUsuarios.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        btnRegistrarRostro.setEnabled(false);
        btnEliminarRostro.setEnabled(false);
        lblEstadoRostro.setText("Seleccione un usuario.");
        actualizarTextoEstado(true);
    }

    private void registrarRostro() {
        if (usuarioSeleccionado == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }
        int userId = usuarioSeleccionado.getIdUsuario();
        boolean reemplazar = perfilFacialRegistrado;
        String accion = reemplazar ? "actualizar" : "registrar";
        if (!confirmar("¿Desea " + accion + " el rostro de " + usuarioSeleccionado.getUsuario() + "?",
                "Registro facial")) return;
        btnRegistrarRostro.setEnabled(false);
        btnEliminarRostro.setEnabled(false);
        lblEstadoRostro.setText("Cámara activa. Siga las instrucciones...");

        new SwingWorker<FacialEnrollmentResult, Void>() {
            @Override
            protected FacialEnrollmentResult doInBackground() {
                return facialAuthClient.enroll(userId, reemplazar);
            }

            @Override
            protected void done() {
                try {
                    FacialEnrollmentResult result = get();
                    JOptionPane.showMessageDialog(panelPrincipal,
                            result.message(), "Registro facial", JOptionPane.INFORMATION_MESSAGE);
                    consultarEstadoRostro(userId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("El registro facial fue interrumpido.");
                } catch (ExecutionException e) {
                    mostrarError(mensajeError(e.getCause(), "No fue posible registrar el rostro."));
                    consultarEstadoRostro(userId);
                }
            }
        }.execute();
    }

    private void eliminarRostro() {
        if (usuarioSeleccionado == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }
        if (usuarioAutenticado == null || usuarioAutenticado.getRol() != RolUsuario.ADMIN) {
            mostrarError("Solo un administrador puede eliminar perfiles faciales.");
            return;
        }
        int userId = usuarioSeleccionado.getIdUsuario();
        if (!confirmar("¿Desea eliminar el perfil facial de " + usuarioSeleccionado.getUsuario() + "?",
                "Eliminar perfil facial")) return;
        btnRegistrarRostro.setEnabled(false);
        btnEliminarRostro.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                facialAuthClient.deleteProfile(userId, usuarioAutenticado.getIdUsuario());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(panelPrincipal,
                            "Perfil facial eliminado correctamente.", "Perfil facial",
                            JOptionPane.INFORMATION_MESSAGE);
                    consultarEstadoRostro(userId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("La eliminación del perfil fue interrumpida.");
                } catch (ExecutionException e) {
                    mostrarError(mensajeError(e.getCause(), "No fue posible eliminar el perfil facial."));
                    consultarEstadoRostro(userId);
                }
            }
        }.execute();
    }

    private void consultarEstadoRostro(int userId) {
        lblEstadoRostro.setText("Consultando perfil facial...");
        btnRegistrarRostro.setEnabled(false);
        btnEliminarRostro.setEnabled(false);
        new SwingWorker<FacialStatus, Void>() {
            @Override
            protected FacialStatus doInBackground() {
                return facialAuthClient.status(userId);
            }

            @Override
            protected void done() {
                if (usuarioSeleccionado == null || usuarioSeleccionado.getIdUsuario() != userId) return;
                try {
                    FacialStatus status = get();
                    perfilFacialRegistrado = status.enrolled();
                    lblEstadoRostro.setText(status.enrolled()
                            ? "Registrado (" + status.model() + ", " + status.samples() + " muestras)"
                            : "No registrado");
                    btnRegistrarRostro.setText(status.enrolled() ? "Actualizar rostro" : "Registrar rostro");
                    btnRegistrarRostro.setEnabled(true);
                    btnEliminarRostro.setEnabled(status.enrolled()
                            && usuarioAutenticado != null
                            && usuarioAutenticado.getRol() == RolUsuario.ADMIN);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    lblEstadoRostro.setText("Estado facial no disponible");
                } catch (ExecutionException e) {
                    perfilFacialRegistrado = false;
                    lblEstadoRostro.setText("Servicio facial no disponible");
                    mostrarError(mensajeError(e.getCause(), "No fue posible consultar el perfil facial."));
                }
            }
        }.execute();
    }

    private String mensajeError(Throwable error, String defecto) {
        if (error instanceof FacialClientException facial && facial.getMessage() != null) {
            return facial.getMessage();
        }
        return defecto;
    }

    private void actualizarTextoEstado(boolean activo) {
        btnDesactivar.setText(activo ? "Desactivar" : "Activar");
    }

    private boolean esUsuarioAutenticadoActivo(Usuario usuario) {
        return usuarioAutenticado != null && usuario != null
                && usuario.getIdUsuario() == usuarioAutenticado.getIdUsuario()
                && usuario.isActivo();
    }

    private boolean confirmar(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(panelPrincipal, mensaje, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void mostrarError(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado.";
        JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Usuarios", JOptionPane.ERROR_MESSAGE);
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Desea regresar al menú principal?", "Regresar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            java.awt.Window ventana = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (ventana != null) ventana.dispose();
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}
