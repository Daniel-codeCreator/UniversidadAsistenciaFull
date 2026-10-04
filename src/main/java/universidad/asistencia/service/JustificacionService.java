package universidad.asistencia.service;

import universidad.asistencia.enums.EstadoJustificacion;
import universidad.asistencia.model.Justificacion;
import universidad.asistencia.repository.*;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Optional;

public class JustificacionService {
    private final JustificacionRepository repository;
    private final EstudianteRepository estudianteRepository;
    private final SesionClaseRepository sesionRepository;
    private final DocenteRepository docenteRepository;
    private final InscripcionRepository inscripcionRepository;
    public JustificacionService() { this(new JustificacionRepository(), new EstudianteRepository(), new SesionClaseRepository(), new DocenteRepository(), new InscripcionRepository()); }
    public JustificacionService(JustificacionRepository repository, EstudianteRepository estudianteRepository, SesionClaseRepository sesionRepository, DocenteRepository docenteRepository, InscripcionRepository inscripcionRepository) { this.repository = repository; this.estudianteRepository = estudianteRepository; this.sesionRepository = sesionRepository; this.docenteRepository = docenteRepository; this.inscripcionRepository = inscripcionRepository; }

    public boolean registrarJustificacion(Justificacion j) {
        validar(j);
        if (repository.existe(j.getEstudiante().getIdEstudiante(), j.getSesionClase().getIdSesion())) throw new BusinessException("Ya existe una justificación para ese estudiante y sesión.");
        j.setEstado(EstadoJustificacion.PENDIENTE);
        return repository.guardar(j);
    }
    public Optional<Justificacion> buscar(int id) { return repository.buscarPorId(id); }
    public List<Justificacion> listar() { return repository.listar(); }
    public List<Justificacion> listarPorEstado(EstadoJustificacion estado) { return repository.listarPorEstado(estado); }
    public Justificacion aprobarJustificacion(int idJustificacion, int idDocenteResuelve) { return resolver(idJustificacion, idDocenteResuelve, EstadoJustificacion.APROBADA); }
    public Justificacion rechazarJustificacion(int idJustificacion, int idDocenteResuelve) { return resolver(idJustificacion, idDocenteResuelve, EstadoJustificacion.RECHAZADA); }

    private Justificacion resolver(int id, int idDocente, EstadoJustificacion estado) {
        Justificacion j = repository.buscarPorId(id).orElseThrow(() -> new BusinessException("La justificación no existe."));
        if (j.getEstado() != EstadoJustificacion.PENDIENTE) throw new BusinessException("Solo se puede resolver una justificación pendiente.");
        if (!docenteRepository.buscarPorId(idDocente).map(d -> d.isActivo()).orElse(false)) throw new BusinessException("El docente resolutor no existe o está inactivo.");
        if (!repository.resolver(id, estado, idDocente)) throw new BusinessException("No se pudo resolver la justificación.");
        return repository.buscarPorId(id).orElseThrow(() -> new BusinessException("No se pudo leer la justificación resuelta."));
    }
    private void validar(Justificacion j) {
        if (j == null) throw new BusinessException("La justificación es obligatoria.");
        if (j.getEstudiante() == null || !estudianteRepository.buscarPorId(j.getEstudiante().getIdEstudiante()).isPresent()) throw new BusinessException("El estudiante de la justificación no existe.");
        if (j.getSesionClase() == null) throw new BusinessException("La sesión de la justificación es obligatoria.");
        var sesion = sesionRepository.buscarPorId(j.getSesionClase().getIdSesion()).orElseThrow(() -> new BusinessException("La sesión de la justificación no existe."));
        if (!inscripcionRepository.buscarVigente(j.getEstudiante().getIdEstudiante(), sesion.getSeccion().getIdSeccion(), sesion.getFecha()).isPresent()) throw new BusinessException("El estudiante no está inscrito en la sesión.");
        if (j.getDocenteRegistra() == null || !docenteRepository.buscarPorId(j.getDocenteRegistra().getIdDocente()).map(d -> d.isActivo()).orElse(false)) throw new BusinessException("El docente que registra no existe o está inactivo.");
        if (j.getMotivo() == null || j.getMotivo().isBlank()) throw new BusinessException("El motivo es obligatorio.");
    }
}
