package universidad.asistencia.view;

import universidad.asistencia.controller.CursoController;
import universidad.asistencia.model.Curso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class CursoView {

    // Componentes vinculados desde CursoForm.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtCreditos;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblCursos;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final CursoController cursoController;


    /*
     * Constructor normal.
     */
    public CursoView() {

        this(new CursoController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public CursoView(
            CursoController cursoController
    ) {

        this.cursoController = cursoController;

        configurarFormulario();
        configurarEventos();
        cargarCursos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        // Todo curso nuevo inicia activo.
        chkActivo.setSelected(true);

        /*
         * El estado no se cambia directamente desde el checkbox.
         * Se cambia utilizando el botón Activar/Desactivar.
         */
        chkActivo.setEnabled(false);

        btnGuardar.setEnabled(true);

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
                                "Código",
                                "Nombre",
                                "Descripción",
                                "Créditos",
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

        tblCursos.setModel(modelo);

        tblCursos.setSelectionMode(
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
         * recuperamos el curso utilizando su ID.
         */
        tblCursos
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarCurso();
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

            int creditos =
                    Integer.parseInt(
                            txtCreditos.getText().trim()
                    );

            Curso curso =
                    new Curso(
                            txtCodigo.getText().trim(),
                            txtNombre.getText().trim(),
                            txtDescripcion.getText().trim(),
                            creditos
                    );

            boolean guardado =
                    cursoController.guardar(
                            curso
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Curso guardado correctamente.",
                        "Curso",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarCursos();

            } else {

                mostrarError(
                        "No se pudo guardar el curso."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Los créditos deben ser un valor numérico."
            );

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
    private void cargarCursos() {

        try {

            List<Curso> cursos =
                    cursoController.listar();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblCursos.getModel();

            modelo.setRowCount(0);

            for (Curso curso : cursos) {

                modelo.addRow(
                        new Object[]{
                                curso.getIdCurso(),
                                curso.getCodigo(),
                                curso.getNombre(),
                                curso.getDescripcion(),
                                curso.getCreditos(),

                                curso.isActivo()
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
                    "Debe seleccionar un curso.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea actualizar este curso?", "Confirmar actualización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        try {

            int idCurso =
                    Integer.parseInt(
                            txtId.getText()
                    );

            int creditos =
                    Integer.parseInt(
                            txtCreditos.getText().trim()
                    );

            Curso curso =
                    new Curso(
                            idCurso,
                            txtCodigo.getText().trim(),
                            txtNombre.getText().trim(),
                            txtDescripcion.getText().trim(),
                            creditos,

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected()
                    );

            boolean actualizado =
                    cursoController.actualizar(
                            curso
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Curso actualizado correctamente.",
                        "Curso",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarCursos();

            } else {

                mostrarError(
                        "No se pudo actualizar el curso."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Los créditos deben ser un valor numérico."
            );

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
                    "Debe seleccionar un curso.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        /*
         * El checkbox refleja el estado actual
         * del curso seleccionado.
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
                                + " el curso?",
                        "Confirmar "
                                + accionTitulo,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int idCurso =
                    Integer.parseInt(
                            txtId.getText()
                    );

            int creditos =
                    Integer.parseInt(
                            txtCreditos.getText().trim()
                    );

            /*
             * Construimos el curso con todos sus datos,
             * cambiando únicamente el atributo activo.
             */
            Curso curso =
                    new Curso(
                            idCurso,
                            txtCodigo.getText().trim(),
                            txtNombre.getText().trim(),
                            txtDescripcion.getText().trim(),
                            creditos,
                            nuevoEstado
                    );

            /*
             * No necesitamos crear un nuevo método en el
             * Controller porque actualizar() ya persiste
             * el campo activo.
             */
            boolean actualizado =
                    cursoController.actualizar(
                            curso
                    );

            if (actualizado) {

                String mensaje =
                        nuevoEstado
                                ? "Curso activado correctamente."
                                : "Curso desactivado correctamente.";

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        mensaje,
                        "Curso",
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

                cargarCursos();

                /*
                 * Volvemos a seleccionar visualmente
                 * el mismo curso.
                 */
                seleccionarFilaPorId(
                        idCurso
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "del curso."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Los créditos deben ser un valor numérico."
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Cambia el texto del botón dependiendo
     * del estado actual del curso.
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
     * BUSCAR POR CÓDIGO
     * ========================================================
     */
    private void buscar() {

        String codigo =
                txtBuscar.getText().trim();

        /*
         * Si no escribió nada, mostramos nuevamente
         * todos los cursos.
         */
        if (codigo.isBlank()) {

            cargarCursos();

            return;
        }

        try {

            Optional<Curso> resultado =
                    cursoController
                            .buscarPorCodigo(codigo);

            if (resultado.isPresent()) {

                Curso curso =
                        resultado.get();

                mostrarCursoEnFormulario(
                        curso
                );

                seleccionarFilaPorId(
                        curso.getIdCurso()
                );

            } else {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontró ningún curso "
                                + "con ese código.",
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
    private void seleccionarCurso() {

        int fila =
                tblCursos.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idCurso =
                    Integer.parseInt(
                            tblCursos
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Curso> resultado =
                    cursoController.buscar(
                            idCurso
                    );

            if (resultado.isPresent()) {

                mostrarCursoEnFormulario(
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
    private void mostrarCursoEnFormulario(
            Curso curso
    ) {

        txtId.setText(
                String.valueOf(
                        curso.getIdCurso()
                )
        );

        txtCodigo.setText(
                curso.getCodigo()
        );

        txtNombre.setText(
                curso.getNombre()
        );

        txtDescripcion.setText(
                curso.getDescripcion()
        );

        txtCreditos.setText(
                String.valueOf(
                        curso.getCreditos()
                )
        );

        /*
         * Reflejamos el estado real.
         */
        chkActivo.setSelected(
                curso.isActivo()
        );

        /*
         * Cambiamos automáticamente el texto del botón.
         */
        actualizarTextoBotonEstado(
                curso.isActivo()
        );

        /*
         * Estamos trabajando con un registro existente.
         */
        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        /*
         * El botón debe estar habilitado tanto para
         * cursos activos como inactivos.
         */
        btnDesactivar.setEnabled(true);
    }


    /*
     * Busca visualmente en la JTable el registro
     * seleccionado por una búsqueda.
     */
    private void seleccionarFilaPorId(
            int idCurso
    ) {

        for (
                int fila = 0;
                fila < tblCursos.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblCursos
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idCurso) {

                tblCursos.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblCursos.scrollRectToVisible(
                        tblCursos
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

        txtCodigo.requestFocus();
    }


    /*
     * Limpia la pantalla para ingresar un nuevo curso.
     */
    private void limpiarFormulario() {

        txtId.setText("");

        txtCodigo.setText("");

        txtNombre.setText("");

        txtDescripcion.setText("");

        txtCreditos.setText("");

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblCursos.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);

        /*
         * Para un nuevo curso el estado inicial
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
     * Conservamos exactamente la lógica utilizada:
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
