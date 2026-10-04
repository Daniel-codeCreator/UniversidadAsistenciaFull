package universidad.asistencia.view;

import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.model.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class EstudianteView {

    // Componentes vinculados desde EstudianteView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCarnet;
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

    private JTable tblEstudiantes;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final EstudianteController estudianteController;


    /*
     * Constructor normal.
     */
    public EstudianteView() {

        this(new EstudianteController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public EstudianteView(
            EstudianteController estudianteController
    ) {

        this.estudianteController = estudianteController;

        configurarFormulario();
        configurarEventos();
        cargarEstudiantes();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        // Todo estudiante nuevo inicia activo.
        chkActivo.setSelected(true);

        /*
         * El estado no se cambia directamente desde el checkbox.
         * Se cambia utilizando el botón Activar/Desactivar.
         */
        chkActivo.setEnabled(false);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);
        btnDesactivar.setText("Desactivar");

        configurarTabla();
    }


    /*
     * Configuración de JTable.
     */
    private void configurarTabla() {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Carnet",
                                "Nombres",
                                "Apellidos",
                                "Correo",
                                "Activo"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tblEstudiantes.setModel(modelo);

        tblEstudiantes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
    }


    /*
     * Eventos de los controles.
     */
    private void configurarEventos() {

        btnNuevo.addActionListener(
                e -> nuevo()
        );

        btnGuardar.addActionListener(
                e -> guardar()
        );

        btnActualizar.addActionListener(
                e -> actualizar()
        );

        /*
         * El mismo botón permite activar y desactivar.
         */
        btnDesactivar.addActionListener(
                e -> cambiarEstado()
        );

        btnBuscar.addActionListener(
                e -> buscar()
        );

        btnSalir.addActionListener(
                e -> salir()
        );

        /*
         * Cuando seleccionamos una fila,
         * recuperamos el estudiante utilizando su ID.
         */
        tblEstudiantes
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarEstudiante();
                    }
                });
    }


    /*
     * ========================================================
     * CREATE
     * ========================================================
     */
    private void guardar() {

        try {

            Estudiante estudiante =
                    new Estudiante(
                            txtCarnet.getText().trim(),
                            txtNombres.getText().trim(),
                            txtApellidos.getText().trim(),
                            txtCorreo.getText().trim()
                    );

            boolean guardado =
                    estudianteController.guardar(
                            estudiante
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Estudiante guardado correctamente.\n"
                                + "ID generado: "
                                + estudiante.getIdEstudiante(),
                        "Estudiante",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarEstudiantes();

            } else {

                mostrarError(
                        "No se pudo guardar el estudiante."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * READ - LISTAR
     * ========================================================
     */
    private void cargarEstudiantes() {

        try {

            List<Estudiante> estudiantes =
                    estudianteController.listar();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblEstudiantes.getModel();

            modelo.setRowCount(0);

            for (Estudiante estudiante : estudiantes) {

                modelo.addRow(
                        new Object[]{
                                estudiante.getIdEstudiante(),
                                estudiante.getCarnet(),
                                estudiante.getNombres(),
                                estudiante.getApellidos(),
                                estudiante.getCorreo(),

                                estudiante.isActivo()
                                        ? "Sí"
                                        : "No"
                        }
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * UPDATE
     * ========================================================
     */
    private void actualizar() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un estudiante.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea actualizar este estudiante?", "Confirmar actualización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        try {

            int idEstudiante =
                    Integer.parseInt(
                            txtId.getText()
                    );

            Estudiante estudiante =
                    new Estudiante(
                            idEstudiante,
                            txtCarnet.getText().trim(),
                            txtNombres.getText().trim(),
                            txtApellidos.getText().trim(),
                            txtCorreo.getText().trim(),

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected()
                    );

            boolean actualizado =
                    estudianteController.actualizar(
                            estudiante
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Estudiante actualizado correctamente.",
                        "Estudiante",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarEstudiantes();

            } else {

                mostrarError(
                        "No se pudo actualizar el estudiante."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * CAMBIAR ESTADO
     *
     * ACTIVO   -> INACTIVO
     * INACTIVO -> ACTIVO
     *
     * Utilizamos actualizar() porque el Repository ya guarda
     * el valor booleano del atributo activo.
     * ========================================================
     */
    private void cambiarEstado() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un estudiante.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        /*
         * El checkbox refleja el estado actual
         * del estudiante seleccionado.
         */
        boolean estadoActual =
                chkActivo.isSelected();

        boolean nuevoEstado =
                !estadoActual;


        /*
         * Los textos cambian dependiendo del estado actual.
         */
        String accion =
                nuevoEstado
                        ? "activar"
                        : "desactivar";

        String accionTitulo =
                nuevoEstado
                        ? "Activar"
                        : "Desactivar";


        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Está seguro de "
                                + accion
                                + " al estudiante?",
                        "Confirmar "
                                + accionTitulo,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }


        try {

            int idEstudiante =
                    Integer.parseInt(
                            txtId.getText()
                    );


            /*
             * Construimos el estudiante con todos sus datos,
             * cambiando únicamente el atributo activo.
             */
            Estudiante estudiante =
                    new Estudiante(
                            idEstudiante,
                            txtCarnet.getText().trim(),
                            txtNombres.getText().trim(),
                            txtApellidos.getText().trim(),
                            txtCorreo.getText().trim(),
                            nuevoEstado
                    );


            /*
             * No necesitamos crear un nuevo método en el
             * Controller porque actualizar() ya persiste
             * el campo activo.
             */
            boolean actualizado =
                    estudianteController.actualizar(
                            estudiante
                    );


            if (actualizado) {

                String mensaje =
                        nuevoEstado
                                ? "Estudiante activado correctamente."
                                : "Estudiante desactivado correctamente.";


                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        mensaje,
                        "Estudiante",
                        JOptionPane.INFORMATION_MESSAGE
                );


                /*
                 * Reflejamos inmediatamente el nuevo estado.
                 */
                chkActivo.setSelected(
                        nuevoEstado
                );


                actualizarTextoBotonEstado(
                        nuevoEstado
                );


                cargarEstudiantes();


                /*
                 * Volvemos a seleccionar visualmente
                 * el mismo estudiante.
                 */
                seleccionarFilaPorId(
                        idEstudiante
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "del estudiante."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Cambia el texto del botón dependiendo
     * del estado actual del estudiante.
     *
     * Activo:
     *      botón = Desactivar
     *
     * Inactivo:
     *      botón = Activar
     */
    private void actualizarTextoBotonEstado(
            boolean activo
    ) {

        if (activo) {

            btnDesactivar.setText(
                    "Desactivar"
            );

        } else {

            btnDesactivar.setText(
                    "Activar"
            );
        }
    }


    /*
     * ========================================================
     * BUSCAR POR CARNET
     * ========================================================
     */
    private void buscar() {

        String carnet =
                txtBuscar.getText().trim();

        /*
         * Si no escribió nada, mostramos nuevamente
         * todos los estudiantes.
         */
        if (carnet.isBlank()) {

            cargarEstudiantes();

            return;
        }

        try {

            Optional<Estudiante> resultado =
                    estudianteController
                            .buscarPorCarnet(carnet);

            if (resultado.isPresent()) {

                Estudiante estudiante =
                        resultado.get();

                mostrarEstudianteEnFormulario(
                        estudiante
                );

                seleccionarFilaPorId(
                        estudiante.getIdEstudiante()
                );

            } else {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontró ningún estudiante "
                                + "con ese carnet.",
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * SELECCIÓN DESDE JTable
     * ========================================================
     */
    private void seleccionarEstudiante() {

        int fila =
                tblEstudiantes.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idEstudiante =
                    Integer.parseInt(
                            tblEstudiantes
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Estudiante> resultado =
                    estudianteController.buscar(
                            idEstudiante
                    );

            if (resultado.isPresent()) {

                mostrarEstudianteEnFormulario(
                        resultado.get()
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Coloca la información del objeto en los controles.
     */
    private void mostrarEstudianteEnFormulario(
            Estudiante estudiante
    ) {

        txtId.setText(
                String.valueOf(
                        estudiante.getIdEstudiante()
                )
        );

        txtCarnet.setText(
                estudiante.getCarnet()
        );

        txtNombres.setText(
                estudiante.getNombres()
        );

        txtApellidos.setText(
                estudiante.getApellidos()
        );

        txtCorreo.setText(
                estudiante.getCorreo()
        );


        /*
         * Reflejamos el estado real.
         */
        chkActivo.setSelected(
                estudiante.isActivo()
        );


        /*
         * Cambiamos automáticamente el texto del botón.
         */
        actualizarTextoBotonEstado(
                estudiante.isActivo()
        );


        /*
         * Estamos trabajando con un registro existente.
         */
        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);


        /*
         * IMPORTANTE:
         *
         * El botón debe estar habilitado tanto para
         * estudiantes activos como inactivos.
         *
         * Si está activo permitirá Desactivar.
         * Si está inactivo permitirá Activar.
         */
        btnDesactivar.setEnabled(true);
    }


    /*
     * Busca visualmente en la JTable el registro
     * seleccionado por una búsqueda.
     */
    private void seleccionarFilaPorId(
            int idEstudiante
    ) {

        for (
                int fila = 0;
                fila < tblEstudiantes.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblEstudiantes
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idEstudiante) {

                tblEstudiantes.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblEstudiantes.scrollRectToVisible(
                        tblEstudiantes
                                .getCellRect(
                                        fila,
                                        0,
                                        true
                                )
                );

                break;
            }
        }
    }


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {

        limpiarFormulario();

        txtCarnet.requestFocus();
    }


    /*
     * Limpia la pantalla para ingresar un nuevo estudiante.
     */
    private void limpiarFormulario() {

        txtId.setText("");

        txtCarnet.setText("");

        txtNombres.setText("");

        txtApellidos.setText("");

        txtCorreo.setText("");

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblEstudiantes.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);

        /*
         * Para un nuevo estudiante el estado inicial
         * siempre es activo.
         */
        btnDesactivar.setText(
                "Desactivar"
        );
    }


    /*
     * Todos los errores de la View pasan por un único método.
     */
    private void mostrarError(
            String mensaje
    ) {

        if (
                mensaje == null
                        || mensaje.isBlank()
        ) {

            mensaje =
                    "Ocurrió un error inesperado.";
        }

        JOptionPane.showMessageDialog(
                panelPrincipal,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }


    /*
     * Conservamos exactamente la lógica que utilizaste:
     * salir de este catálogo significa regresar al MainForm.
     */
    private void salir() {

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Desea regresar al menú principal?",
                        "Regresar",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta == JOptionPane.YES_OPTION) {

            // Obtiene la ventana JFrame que contiene este JPanel
            // y cierra únicamente el catálogo actual.
            java.awt.Window ventanaActual =
                    SwingUtilities.getWindowAncestor(
                            panelPrincipal
                    );

            if (ventanaActual != null) {
                ventanaActual.dispose();
            }
        }
    }


    /*
     * El JFrame principal utilizará este JPanel.
     */
    public JPanel getPanelPrincipal() {

        return panelPrincipal;
    }
}
