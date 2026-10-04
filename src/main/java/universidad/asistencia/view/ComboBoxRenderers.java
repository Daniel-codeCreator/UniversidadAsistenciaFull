package universidad.asistencia.view;

import universidad.asistencia.enums.EstadoJustificacion;
import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.enums.ResultadoAsistencia;
import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.enums.TipoMarcaje;
import universidad.asistencia.model.Curso;
import universidad.asistencia.model.Dispositivo;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.model.Seccion;
import universidad.asistencia.model.SesionClase;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

/** Presentación legible para combos sin alterar el modelo de dominio. */
public final class ComboBoxRenderers {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ComboBoxRenderers() {
    }

    public static void configurar(JComboBox<Curso> comboCurso) {
        comboCurso.setRenderer(renderer(ComboBoxRenderers::curso));
    }

    public static void configurarPeriodos(JComboBox<PeriodoAcademico> comboPeriodo) {
        comboPeriodo.setRenderer(renderer(ComboBoxRenderers::periodo));
    }

    public static void configurarDocentes(JComboBox<Docente> comboDocente) {
        comboDocente.setRenderer(renderer(ComboBoxRenderers::docente));
    }

    public static void configurarEstudiantes(JComboBox<Estudiante> comboEstudiante) {
        comboEstudiante.setRenderer(renderer(ComboBoxRenderers::estudiante));
    }

    public static void configurarSecciones(JComboBox<Seccion> comboSeccion) {
        comboSeccion.setRenderer(renderer(ComboBoxRenderers::seccion));
    }

    public static void configurarHorarios(JComboBox<HorarioSemanal> comboHorario) {
        comboHorario.setRenderer(renderer(ComboBoxRenderers::horario));
    }

    public static void configurarSesiones(JComboBox<SesionClase> comboSesion) {
        comboSesion.setRenderer(renderer(ComboBoxRenderers::sesion));
    }

    public static void configurarDispositivos(JComboBox<Dispositivo> comboDispositivo) {
        comboDispositivo.setRenderer(renderer(ComboBoxRenderers::dispositivo));
    }

    public static void configurarDias(JComboBox<String> comboDia) {
        comboDia.setRenderer(renderer((String valor) -> valor));
    }

    public static void configurarEstadosSesion(JComboBox<EstadoSesion> combo) {
        combo.setRenderer(renderer(ComboBoxRenderers::enumTexto));
    }

    public static void configurarMedios(JComboBox<MedioMarcaje> combo) {
        combo.setRenderer(renderer(ComboBoxRenderers::enumTexto));
    }

    public static void configurarRoles(JComboBox<RolUsuario> combo) {
        combo.setRenderer(renderer(ComboBoxRenderers::enumTexto));
    }

    public static void configurarTiposMarcaje(JComboBox<TipoMarcaje> combo) {
        combo.setRenderer(renderer(ComboBoxRenderers::enumTexto));
    }

    public static void configurarResultados(JComboBox<ResultadoAsistencia> combo) {
        combo.setRenderer(renderer(ComboBoxRenderers::enumTexto));
    }

    public static void configurarEstadosJustificacion(JComboBox<EstadoJustificacion> combo) {
        combo.setRenderer(renderer(ComboBoxRenderers::enumTexto));
    }

    public static String texto(Enum<?> valor) {
        return valor == null ? "" : enumTexto(valor);
    }

    public static String fecha(LocalDate valor) {
        return valor == null ? "" : valor.format(FECHA);
    }

    public static String fechaHora(LocalDateTime valor) {
        return valor == null ? "" : valor.format(FECHA_HORA);
    }

    public static String hora(LocalTime valor) {
        return valor == null ? "" : valor.format(HORA);
    }

    public static String medio(String valor) {
        if (valor == null || valor.isBlank()) return "";
        try {
            return texto(MedioMarcaje.valueOf(valor));
        } catch (IllegalArgumentException e) {
            return valor;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> DefaultListCellRenderer renderer(Function<T, String> formatter) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "-- Seleccione --" : formatter.apply((T) value));
                return this;
            }
        };
    }

    private static String curso(Curso curso) {
        return unir(curso.getCodigo(), curso.getNombre());
    }

    private static String periodo(PeriodoAcademico periodo) {
        return periodo.getNombre();
    }

    private static String docente(Docente docente) {
        return unir(docente.getCodigoEmpleado(), unir(docente.getNombres(), docente.getApellidos()));
    }

    private static String estudiante(Estudiante estudiante) {
        return unir(estudiante.getCarnet(), unir(estudiante.getNombres(), estudiante.getApellidos()));
    }

    private static String seccion(Seccion seccion) {
        String curso = seccion.getCurso() == null ? "" : curso(seccion.getCurso());
        String periodo = seccion.getPeriodoAcademico() == null ? "" : seccion.getPeriodoAcademico().getNombre();
        return unir(seccion.getCodigo(), unir(curso, periodo));
    }

    private static String horario(HorarioSemanal horario) {
        String seccion = horario.getSeccion() == null ? "" : horario.getSeccion().getCodigo();
        String dia = horario.getDiaSemana() == null ? "" : dia(horario.getDiaSemana().getValue());
        String horas = horario.getHoraInicio() == null || horario.getHoraFin() == null
                ? "" : horario.getHoraInicio().format(HORA) + " - " + horario.getHoraFin().format(HORA);
        return unir(seccion, unir(dia, horas));
    }

    private static String sesion(SesionClase sesion) {
        String seccion = sesion.getSeccion() == null ? "" : sesion.getSeccion().getCodigo();
        String fecha = fecha(sesion.getFecha());
        String estado = sesion.getEstado() == null ? "" : enumTexto(sesion.getEstado());
        return unir(fecha, unir(seccion, estado));
    }

    private static String dispositivo(Dispositivo dispositivo) {
        return unir(dispositivo.getCodigo(), dispositivo.getNombre());
    }

    private static String enumTexto(Enum<?> valor) {
        if (valor instanceof MedioMarcaje medio && medio == MedioMarcaje.LECTOR_AULA) return "Lector de aula";
        if (valor instanceof MedioMarcaje medio && medio == MedioMarcaje.APP_MOVIL) return "App móvil";
        if (valor instanceof RolUsuario rol && rol == RolUsuario.ADMIN) return "Administrador";
        String texto = valor.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String dia(int valor) {
        return switch (valor) {
            case 1 -> "Lunes";
            case 2 -> "Martes";
            case 3 -> "Miércoles";
            case 4 -> "Jueves";
            case 5 -> "Viernes";
            case 6 -> "Sábado";
            case 7 -> "Domingo";
            default -> "";
        };
    }

    private static String unir(String izquierda, String derecha) {
        if (izquierda == null || izquierda.isBlank()) return derecha == null ? "" : derecha;
        if (derecha == null || derecha.isBlank()) return izquierda;
        return izquierda + " - " + derecha;
    }
}
