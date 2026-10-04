package universidad.asistencia.service;

import universidad.asistencia.enums.*;
import universidad.asistencia.model.*;
import universidad.asistencia.repository.*;
import universidad.asistencia.util.BusinessException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsistenciaService {
    private final EstudianteRepository estudianteRepository;
    private final SesionClaseRepository sesionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final HorarioSemanalRepository horarioRepository;
    private final DispositivoRepository dispositivoRepository;
    private final MarcajeRepository marcajeRepository;

    public AsistenciaService() {
        this(new EstudianteRepository(), new SesionClaseRepository(), new InscripcionRepository(),
                new HorarioSemanalRepository(), new DispositivoRepository(), new MarcajeRepository());
    }

    public AsistenciaService(EstudianteRepository estudianteRepository, SesionClaseRepository sesionRepository,
                             InscripcionRepository inscripcionRepository, HorarioSemanalRepository horarioRepository,
                             DispositivoRepository dispositivoRepository, MarcajeRepository marcajeRepository) {
        this.estudianteRepository = estudianteRepository;
        this.sesionRepository = sesionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.horarioRepository = horarioRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.marcajeRepository = marcajeRepository;
    }

    public Marcaje registrarMarcaje(int idEstudiante, int idSesion, int idDispositivo,
                                    TipoMarcaje tipo, MedioMarcaje medio) {
        return registrarMarcaje(idEstudiante, idSesion, idDispositivo, tipo, medio, null);
    }

    /** La variante con fecha permite cargar o probar marcajes históricos deterministas. */
    public Marcaje registrarMarcaje(int idEstudiante, int idSesion, int idDispositivo,
                                    TipoMarcaje tipo, MedioMarcaje medio, LocalDateTime fechaHora) {
        LocalDateTime momento = fechaHora == null ? LocalDateTime.now() : fechaHora;
        validarPuedeMarcar(idEstudiante, idSesion, idDispositivo, tipo, medio, momento);
        Estudiante estudiante = estudianteRepository.buscarPorId(idEstudiante).orElseThrow(() -> new BusinessException("El estudiante no existe."));
        SesionClase sesion = sesionRepository.buscarPorId(idSesion).orElseThrow(() -> new BusinessException("La sesión no existe."));
        Dispositivo dispositivo = dispositivoRepository.buscarPorId(idDispositivo).orElseThrow(() -> new BusinessException("El dispositivo no existe."));
        Marcaje marcaje = new Marcaje(estudiante, sesion, dispositivo, fechaHora, tipo, medio);
        if (fechaHora == null) marcajeRepository.guardar(marcaje); else marcajeRepository.guardarConFecha(marcaje);
        return marcaje;
    }

    public boolean validarPuedeMarcar(int idEstudiante, int idSesion, int idDispositivo,
                                     TipoMarcaje tipo, MedioMarcaje medio) {
        validarPuedeMarcar(idEstudiante, idSesion, idDispositivo, tipo, medio, LocalDateTime.now());
        return true;
    }

    public ResultadoAsistencia calcularResultadoAsistencia(int idEstudiante, int idSesion) {
        SesionClase sesion = sesionRepository.buscarPorId(idSesion).orElseThrow(() -> new BusinessException("La sesión no existe."));
        Optional<Marcaje> primeraEntrada = marcajeRepository.obtenerPrimeraEntrada(idEstudiante, idSesion);
        if (primeraEntrada.isEmpty()) return ResultadoAsistencia.AUSENTE;
        LocalTime entrada = primeraEntrada.get().getFechaHora().toLocalTime();
        if (entrada.isAfter(sesion.getHoraFinProgramada())) return ResultadoAsistencia.AUSENTE;
        return entrada.isAfter(sesion.getHoraInicioProgramada().plusMinutes(10))
                ? ResultadoAsistencia.TARDANZA : ResultadoAsistencia.PRESENTE;
    }

    public double calcularPorcentajeAsistencia(int idEstudiante) {
        List<Inscripcion> inscripciones = inscripcionRepository.listarPorEstudiante(idEstudiante);
        int impartidas = 0;
        int asistidas = 0;
        for (Inscripcion inscripcion : inscripciones) {
            if (!inscripcion.isActiva()) continue;
            for (SesionClase sesion : sesionRepository.listarPorSeccion(inscripcion.getSeccion().getIdSeccion())) {
                if (!sesion.getEstado().equals(EstadoSesion.IMPARTIDA) || !estaDentroDeInscripcion(inscripcion, sesion)) continue;
                impartidas++;
                ResultadoAsistencia resultado = calcularResultadoAsistencia(idEstudiante, sesion.getIdSesion());
                if (resultado == ResultadoAsistencia.PRESENTE || resultado == ResultadoAsistencia.TARDANZA) asistidas++;
            }
        }
        return impartidas == 0 ? 0.0 : asistidas * 100.0 / impartidas;
    }

    public double calcularPorcentajeAsistencia(Estudiante estudiante) {
        if (estudiante == null || estudiante.getIdEstudiante() <= 0) throw new BusinessException("El estudiante no es válido.");
        return calcularPorcentajeAsistencia(estudiante.getIdEstudiante());
    }

    public List<Marcaje> obtenerHistorial(int idEstudiante) { return marcajeRepository.listarHistorial(idEstudiante); }

    public List<Estudiante> obtenerEstudiantesBajo80() {
        return estudianteRepository.listarActivos().stream()
                .filter(e -> calcularPorcentajeAsistencia(e.getIdEstudiante()) < 80.0)
                .collect(Collectors.toList());
    }

    private void validarPuedeMarcar(int idEstudiante, int idSesion, int idDispositivo,
                                    TipoMarcaje tipo, MedioMarcaje medio, LocalDateTime momento) {
        if (tipo == null || medio == null) throw new BusinessException("El tipo y el medio del marcaje son obligatorios.");
        Estudiante estudiante = estudianteRepository.buscarPorId(idEstudiante).orElseThrow(() -> new BusinessException("El estudiante no existe."));
        if (!estudiante.isActivo()) throw new BusinessException("El estudiante está inactivo.");
        SesionClase sesion = sesionRepository.buscarPorId(idSesion).orElseThrow(() -> new BusinessException("La sesión no existe."));
        if (sesion.getEstado() != EstadoSesion.IMPARTIDA) throw new BusinessException("Solo se puede marcar una sesión impartida.");
        if (!inscripcionRepository.buscarVigente(idEstudiante, sesion.getSeccion().getIdSeccion(), sesion.getFecha()).isPresent()) throw new BusinessException("El estudiante no tiene una inscripción vigente para la sesión.");
        if (momento == null || !momento.toLocalDate().equals(sesion.getFecha())) throw new BusinessException("El marcaje debe corresponder a la fecha de la sesión.");
        if (momento.toLocalTime().isAfter(sesion.getHoraFinProgramada())) throw new BusinessException("No se acepta un marcaje posterior al fin de la sesión.");
        boolean horarioValido = horarioRepository.listarPorSeccion(sesion.getSeccion().getIdSeccion()).stream()
                .anyMatch(h -> h.getDiaSemana().getValue() == sesion.getFecha().getDayOfWeek().getValue()
                        && h.getHoraInicio().equals(sesion.getHoraInicioProgramada())
                        && h.getHoraFin().equals(sesion.getHoraFinProgramada()));
        if (!horarioValido) throw new BusinessException("La sesión no coincide con un horario semanal registrado.");
        Dispositivo dispositivo = dispositivoRepository.buscarPorId(idDispositivo).orElseThrow(() -> new BusinessException("El dispositivo no existe."));
        if (!dispositivo.isActivo()) throw new BusinessException("El dispositivo está inactivo.");
        Optional<Marcaje> ultimo = marcajeRepository.obtenerUltimo(idEstudiante, idSesion);
        TipoMarcaje esperado = ultimo.isEmpty() ? TipoMarcaje.ENTRADA :
                (ultimo.get().getTipo() == TipoMarcaje.ENTRADA ? TipoMarcaje.SALIDA : TipoMarcaje.ENTRADA);
        if (tipo != esperado) throw new BusinessException("La secuencia esperada es " + esperado + ".");
    }

    private boolean estaDentroDeInscripcion(Inscripcion i, SesionClase s) {
        return !s.getFecha().isBefore(i.getFechaInscripcion()) &&
                (i.getFechaRetiro() == null || !s.getFecha().isAfter(i.getFechaRetiro()));
    }
}
