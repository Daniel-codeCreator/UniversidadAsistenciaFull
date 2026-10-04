package universidad.asistencia.util;

import universidad.asistencia.enums.*;
import universidad.asistencia.model.*;
import universidad.asistencia.repository.*;
import universidad.asistencia.service.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Carga un escenario pequeño, coherente y repetible para probar las capas Java. */
public class DatosPrueba {
    private final EstudianteRepository estudianteRepository = new EstudianteRepository();
    private final DocenteRepository docenteRepository = new DocenteRepository();
    private final CursoRepository cursoRepository = new CursoRepository();
    private final PeriodoAcademicoRepository periodoRepository = new PeriodoAcademicoRepository();
    private final SeccionRepository seccionRepository = new SeccionRepository();
    private final HorarioSemanalRepository horarioRepository = new HorarioSemanalRepository();
    private final InscripcionRepository inscripcionRepository = new InscripcionRepository();
    private final SesionClaseRepository sesionRepository = new SesionClaseRepository();
    private final DispositivoRepository dispositivoRepository = new DispositivoRepository();
    private final MarcajeRepository marcajeRepository = new MarcajeRepository();
    private final JustificacionRepository justificacionRepository = new JustificacionRepository();
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    private final EstudianteService estudianteService = new EstudianteService(estudianteRepository);
    private final DocenteService docenteService = new DocenteService(docenteRepository);
    private final CursoService cursoService = new CursoService(cursoRepository);
    private final PeriodoAcademicoService periodoService = new PeriodoAcademicoService(periodoRepository);
    private final SeccionService seccionService = new SeccionService(seccionRepository);
    private final HorarioSemanalService horarioService = new HorarioSemanalService(horarioRepository);
    private final InscripcionService inscripcionService = new InscripcionService(inscripcionRepository);
    private final SesionClaseService sesionService = new SesionClaseService(sesionRepository);
    private final DispositivoService dispositivoService = new DispositivoService(dispositivoRepository);
    private final AsistenciaService asistenciaService = new AsistenciaService(estudianteRepository, sesionRepository, inscripcionRepository, horarioRepository, dispositivoRepository, marcajeRepository);
    private final JustificacionService justificacionService = new JustificacionService(justificacionRepository, estudianteRepository, sesionRepository, docenteRepository, inscripcionRepository);
    private final UsuarioService usuarioService = new UsuarioService(usuarioRepository);

    public Escenario cargar() {
        List<Estudiante> estudiantes = cargarEstudiantes();
        List<Docente> docentes = cargarDocentes();
        List<Curso> cursos = cargarCursos();
        List<PeriodoAcademico> periodos = cargarPeriodos();
        List<Seccion> secciones = cargarSecciones(cursos, periodos, docentes);
        List<HorarioSemanal> horarios = cargarHorarios(secciones);
        List<SesionClase> sesiones = cargarSesiones(secciones, horarios);
        cargarInscripciones(estudiantes, secciones);
        List<Dispositivo> dispositivos = cargarDispositivos();
        cargarMarcajes(estudiantes, sesiones, dispositivos);
        cargarJustificaciones(estudiantes, sesiones, docentes);
        cargarUsuarios(docentes);
        return new Escenario(estudiantes, docentes, cursos, periodos, secciones, horarios, sesiones, dispositivos);
    }

    private List<Estudiante> cargarEstudiantes() {
        List<Estudiante> resultado = new ArrayList<>();
        String[][] datos = {
                {"20260001", "Ana", "López García", "ana.lopez@universidad.edu.gt"},
                {"20260002", "Bruno", "Méndez Ruiz", "bruno.mendez@universidad.edu.gt"},
                {"20260003", "Carla", "Pérez Soto", "carla.perez@universidad.edu.gt"},
                {"20260004", "Diego", "Ramírez León", "diego.ramirez@universidad.edu.gt"},
                {"20260005", "Elena", "Castillo Mora", "elena.castillo@universidad.edu.gt"},
                {"20260006", "Fabio", "Herrera Díaz", "fabio.herrera@universidad.edu.gt"},
                {"20260007", "Gabriela", "Villatoro Paz", "gabriela.villatoro@universidad.edu.gt"},
                {"20260008", "Hugo", "Sánchez Ortiz", "hugo.sanchez@universidad.edu.gt"},
                {"20260009", "Irene", "Morales Fuentes", "irene.morales@universidad.edu.gt"},
                {"20260010", "Jorge", "Navarro Cruz", "jorge.navarro@universidad.edu.gt"}
        };
        for (String[] dato : datos) {
            Estudiante estudiante = estudianteRepository.buscarPorCarnet(dato[0]).orElseGet(() -> {
                Estudiante nuevo = new Estudiante(dato[0], dato[1], dato[2], dato[3]);
                estudianteService.guardar(nuevo);
                return nuevo;
            });
            if (!estudiante.isActivo()) {
                estudiante.setActivo(true);
                estudianteService.actualizar(estudiante);
            }
            resultado.add(estudiante);
        }
        return resultado;
    }

    private List<Docente> cargarDocentes() {
        List<Docente> resultado = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            final int indice = i;
            String codigo = String.format("DOC%03d", i);
            Docente docente = docenteRepository.buscarPorCodigo(codigo).orElseGet(() -> {
                Docente nuevo = new Docente(codigo, "Docente " + indice, "Universitario", "docente" + indice + "@universidad.edu.gt");
                docenteService.guardar(nuevo);
                return nuevo;
            });
            resultado.add(docente);
        }
        return resultado;
    }

    private List<Curso> cargarCursos() {
        List<Curso> resultado = new ArrayList<>();
        String[] nombres = {"Programación II", "Bases de Datos", "Matemática Discreta", "Redes de Computadoras", "Ingeniería de Software", "Sistemas Operativos", "Arquitectura de Computadores", "Análisis de Sistemas", "Seguridad Informática", "Gestión de Proyectos"};
        for (int i = 1; i <= 10; i++) {
            final int indice = i;
            String codigo = String.format("CUR%03d", i);
            Curso curso = cursoRepository.buscarPorCodigo(codigo).orElseGet(() -> {
                Curso nuevo = new Curso(codigo, nombres[indice - 1], "Curso de prueba del plan universitario", 3);
                cursoService.guardar(nuevo);
                return nuevo;
            });
            resultado.add(curso);
        }
        return resultado;
    }

    private List<PeriodoAcademico> cargarPeriodos() {
        List<PeriodoAcademico> resultado = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String nombre = String.format("Periodo de Prueba %02d-2026", i);
            PeriodoAcademico periodo = periodoRepository.buscarPorNombre(nombre).orElseGet(() -> {
                PeriodoAcademico nuevo = new PeriodoAcademico(nombre, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
                periodoService.guardar(nuevo);
                return nuevo;
            });
            resultado.add(periodo);
        }
        return resultado;
    }

    private List<Seccion> cargarSecciones(List<Curso> cursos, List<PeriodoAcademico> periodos, List<Docente> docentes) {
        List<Seccion> resultado = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int indice = i;
            Seccion seccion = seccionRepository.buscar(cursos.get(i).getIdCurso(), periodos.get(i).getIdPeriodo(), "A01").orElseGet(() -> {
                Seccion nueva = new Seccion("A01", cursos.get(indice), periodos.get(indice), docentes.get(indice), "Aula " + (indice + 1));
                seccionService.guardar(nueva);
                return nueva;
            });
            resultado.add(seccion);
        }
        return resultado;
    }

    private List<HorarioSemanal> cargarHorarios(List<Seccion> secciones) {
        List<HorarioSemanal> resultado = new ArrayList<>();
        for (int i = 0; i < secciones.size(); i++) {
            final int indice = i;
            DayOfWeek dia = DayOfWeek.of((i % 5) + 1);
            LocalTime inicio = LocalTime.of(7 + (i % 3), 0);
            HorarioSemanal horario = horarioRepository.listarPorSeccion(secciones.get(i).getIdSeccion()).stream().findFirst().orElseGet(() -> {
                HorarioSemanal nuevo = new HorarioSemanal(secciones.get(indice), dia, inicio, inicio.plusHours(2));
                horarioService.guardar(nuevo);
                return nuevo;
            });
            resultado.add(horario);
        }
        return resultado;
    }

    private List<SesionClase> cargarSesiones(List<Seccion> secciones, List<HorarioSemanal> horarios) {
        List<SesionClase> resultado = new ArrayList<>();
        EstadoSesion[] estados = {EstadoSesion.PROGRAMADA, EstadoSesion.IMPARTIDA, EstadoSesion.SUSPENDIDA, EstadoSesion.CANCELADA};
        for (int i = 0; i < secciones.size(); i++) {
            final int indice = i;
            int cantidad = i == 0 ? 5 : 1;
            for (int numero = 0; numero < cantidad; numero++) {
                LocalDate fecha = primeraFechaDelDia(LocalDate.of(2026, 8, 1), horarios.get(i).getDiaSemana()).plusWeeks(numero);
                EstadoSesion estado = i == 0 ? EstadoSesion.IMPARTIDA : estados[(i - 1) % estados.length];
                SesionClase sesion = sesionRepository.buscarPorSeccionFechaHora(secciones.get(i).getIdSeccion(), fecha, horarios.get(i).getHoraInicio()).orElseGet(() -> {
                    SesionClase nueva = new SesionClase(secciones.get(indice), fecha, horarios.get(indice).getHoraInicio(), horarios.get(indice).getHoraFin(), secciones.get(indice).getAulaAsignada(), estado);
                    sesionService.guardar(nueva);
                    return nueva;
                });
                resultado.add(sesion);
            }
        }
        return resultado;
    }

    private void cargarInscripciones(List<Estudiante> estudiantes, List<Seccion> secciones) {
        for (int i = 0; i < estudiantes.size(); i++) {
            int indiceSeccion = i < 5 ? 0 : i;
            if (!inscripcionRepository.existe(estudiantes.get(i).getIdEstudiante(), secciones.get(indiceSeccion).getIdSeccion())) {
                inscripcionService.guardar(new Inscripcion(estudiantes.get(i), secciones.get(indiceSeccion), LocalDate.of(2026, 1, 1)));
            }
        }
        // Esta inscripción adicional permite probar el rechazo de marcajes en una sesión CANCELADA.
        if (!inscripcionRepository.existe(estudiantes.get(4).getIdEstudiante(), secciones.get(4).getIdSeccion())) {
            inscripcionService.guardar(new Inscripcion(estudiantes.get(4), secciones.get(4), LocalDate.of(2026, 1, 1)));
        }
    }

    private List<Dispositivo> cargarDispositivos() {
        List<Dispositivo> resultado = new ArrayList<>();
        MedioMarcaje[] medios = MedioMarcaje.values();
        for (int i = 1; i <= 10; i++) {
            final int indice = i;
            String codigo = String.format("DISP%03d", i);
            Dispositivo dispositivo = dispositivoRepository.buscarPorCodigo(codigo).orElseGet(() -> {
                Dispositivo nuevo = new Dispositivo(codigo, "Dispositivo de prueba " + indice, medios[(indice - 1) % medios.length].name(), "Edificio " + ((indice - 1) % 3 + 1));
                dispositivoService.guardar(nuevo);
                return nuevo;
            });
            resultado.add(dispositivo);
        }
        return resultado;
    }

    private void cargarMarcajes(List<Estudiante> estudiantes, List<SesionClase> sesiones, List<Dispositivo> dispositivos) {
        SesionClase primera = sesiones.get(0);
        for (int i = 0; i < 5; i++) {
            agregarMarcajeSiFalta(estudiantes.get(0), sesiones.get(i), dispositivos.get(i), TipoMarcaje.ENTRADA,
                    LocalDateTime.of(sesiones.get(i).getFecha(), sesiones.get(i).getHoraInicioProgramada().plusMinutes(2)));
            agregarMarcajeSiFalta(estudiantes.get(0), sesiones.get(i), dispositivos.get(i), TipoMarcaje.SALIDA,
                    LocalDateTime.of(sesiones.get(i).getFecha(), sesiones.get(i).getHoraFinProgramada().minusMinutes(5)));
        }
        agregarMarcajeSiFalta(estudiantes.get(1), primera, dispositivos.get(0), TipoMarcaje.ENTRADA,
                LocalDateTime.of(primera.getFecha(), primera.getHoraInicioProgramada().plusMinutes(15)));
        agregarMarcajeSiFalta(estudiantes.get(1), primera, dispositivos.get(0), TipoMarcaje.SALIDA,
                LocalDateTime.of(primera.getFecha(), primera.getHoraFinProgramada().minusMinutes(5)));
    }

    private void agregarMarcajeSiFalta(Estudiante estudiante, SesionClase sesion, Dispositivo dispositivo,
                                       TipoMarcaje tipo, LocalDateTime fechaHora) {
        boolean existe = marcajeRepository.listarPorEstudianteYSesion(estudiante.getIdEstudiante(), sesion.getIdSesion()).stream().anyMatch(m -> m.getTipo() == tipo);
        if (!existe) asistenciaService.registrarMarcaje(estudiante.getIdEstudiante(), sesion.getIdSesion(), dispositivo.getIdDispositivo(), tipo, MedioMarcaje.KIOSCO, fechaHora);
    }

    private void cargarJustificaciones(List<Estudiante> estudiantes, List<SesionClase> sesiones, List<Docente> docentes) {
        for (int i = 0; i < 10; i++) {
            SesionClase sesion = i < 5 ? sesiones.get(i) : sesiones.get(i + 4);
            int estudianteId = estudiantes.get(i).getIdEstudiante();
            if (justificacionRepository.existe(estudianteId, sesion.getIdSesion())) continue;
            Justificacion j = new Justificacion(estudiantes.get(i), sesion, docentes.get(i), "Situación académica documentada", "Registro de prueba para demostrar el flujo de justificaciones.");
            justificacionService.registrarJustificacion(j);
            if (i == 1) justificacionService.aprobarJustificacion(j.getIdJustificacion(), docentes.get(0).getIdDocente());
            if (i == 2) justificacionService.rechazarJustificacion(j.getIdJustificacion(), docentes.get(0).getIdDocente());
        }
    }

    private void cargarUsuarios(List<Docente> docentes) {
        if (!usuarioRepository.existeUsuario("admin.prueba")) usuarioService.crearUsuario("admin.prueba", "Admin2026!", RolUsuario.ADMIN, null);
        for (int i = 0; i < 9; i++) {
            String nombre = String.format("docente.prueba%02d", i + 1);
            if (!usuarioRepository.existeUsuario(nombre)) usuarioService.crearUsuario(nombre, "Docente2026!", RolUsuario.DOCENTE, docentes.get(i));
        }
    }

    private LocalDate primeraFechaDelDia(LocalDate inicio, DayOfWeek dia) {
        int diferencia = dia.getValue() - inicio.getDayOfWeek().getValue();
        if (diferencia < 0) diferencia += 7;
        return inicio.plusDays(diferencia);
    }

    public record Escenario(List<Estudiante> estudiantes, List<Docente> docentes, List<Curso> cursos,
                            List<PeriodoAcademico> periodos, List<Seccion> secciones,
                            List<HorarioSemanal> horarios, List<SesionClase> sesiones,
                            List<Dispositivo> dispositivos) { }
}
