package universidad.asistencia;

import universidad.asistencia.controller.UsuarioController;
import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.view.LoginForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            asegurarUsuarioAdministrador();

            LoginForm loginForm = new LoginForm();
            JFrame frame = new JFrame("Sistema de Asistencia Universitaria");
            frame.setContentPane(loginForm.getPanelPrincipal());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(430, 300);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    /** Crea las credenciales iniciales solo si todavía no existen. */
    private static void asegurarUsuarioAdministrador() {
        try {
            UsuarioController controller = new UsuarioController();
            if (controller.buscarPorUsuario("admin").isEmpty()) {
                controller.crearUsuario("admin", "admin", RolUsuario.ADMIN, null);
            }
        } catch (RuntimeException e) {
            System.err.println("No fue posible verificar el usuario admin: " + e.getMessage());
        }
    }
}
