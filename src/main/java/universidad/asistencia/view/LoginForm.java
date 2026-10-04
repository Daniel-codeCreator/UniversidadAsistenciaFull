package universidad.asistencia.view;

import universidad.asistencia.controller.UsuarioController;
import universidad.asistencia.integration.facial.FacialAuthClient;
import universidad.asistencia.integration.facial.FacialClientException;
import universidad.asistencia.integration.facial.FacialVerificationResult;
import universidad.asistencia.model.Usuario;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.Optional;

/** Pantalla de entrada al sistema. */
public class LoginForm {

    private JPanel panelPrincipal;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnSalir;

    private final UsuarioController usuarioController;
    private final FacialAuthClient facialAuthClient;
    private boolean autenticando;

    public LoginForm() {
        this(new UsuarioController(), new FacialAuthClient());
    }

    public LoginForm(UsuarioController usuarioController) {
        this(usuarioController, new FacialAuthClient());
    }

    public LoginForm(UsuarioController usuarioController, FacialAuthClient facialAuthClient) {
        this.usuarioController = usuarioController;
        this.facialAuthClient = facialAuthClient;
        configurarEventos();
    }

    private void configurarEventos() {
        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> ingresar());
    }

    private void ingresar() {
        if (autenticando) return;
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isBlank()) {
            mostrarError("Ingrese el usuario.");
            txtUsuario.requestFocus();
            return;
        }
        if (password.isBlank()) {
            mostrarError("Ingrese la contraseña.");
            txtPassword.requestFocus();
            return;
        }

        autenticando = true;
        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando identidad...");

        new SwingWorker<LoginOutcome, Void>() {
            @Override
            protected LoginOutcome doInBackground() {
                Optional<Usuario> autenticado = usuarioController.autenticar(usuario, password);
                if (autenticado.isEmpty()) return LoginOutcome.sinCredenciales();
                // El password nunca sale de Java. Python recibe solamente el id estable.
                FacialVerificationResult facial = facialAuthClient.verify(autenticado.get().getIdUsuario());
                return new LoginOutcome(autenticado.get(), facial);
            }

            @Override
            protected void done() {
                try {
                    LoginOutcome outcome = get();
                    if (outcome.credencialesInvalidas()) {
                        mostrarError("Usuario o contraseña incorrectos, o usuario inactivo.");
                        txtPassword.selectAll();
                        txtPassword.requestFocus();
                    } else if (!outcome.facial().matched()) {
                        mostrarError(outcome.facial().message());
                        txtPassword.selectAll();
                        txtPassword.requestFocus();
                    } else {
                        abrirMenu(outcome.usuario());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("La verificación facial fue interrumpida.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof FacialClientException facialError) {
                        mostrarError(facialError.getMessage());
                    } else if (cause instanceof RuntimeException runtimeError) {
                        mostrarError(runtimeError.getMessage());
                    } else {
                        mostrarError("No fue posible completar la autenticación.");
                    }
                } finally {
                    autenticando = false;
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("INGRESAR");
                }
            }
        }.execute();
    }

    private void abrirMenu(Usuario usuario) {
        java.awt.Window ventanaActual = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (ventanaActual != null) ventanaActual.dispose();

        MainForm mainForm = new MainForm(usuario);
        JFrame ventana = new JFrame("Sistema de Asistencia Universitaria");
        ventana.setContentPane(mainForm.getPanelPrincipal());
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(750, 650);
        ventana.setLocationRelativeTo(null);
        ventana.setResizable(false);
        ventana.setVisible(true);
    }

    private void mostrarError(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) mensaje = "Ocurrió un error inesperado.";
        JOptionPane.showMessageDialog(panelPrincipal, mensaje, "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

    private record LoginOutcome(Usuario usuario, FacialVerificationResult facial,
                                boolean credencialesInvalidas) {
        private LoginOutcome(Usuario usuario, FacialVerificationResult facial) {
            this(usuario, facial, false);
        }

        private static LoginOutcome sinCredenciales() {
            return new LoginOutcome(null, null, true);
        }
    }
}
