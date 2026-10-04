package universidad.asistencia.service;

import universidad.asistencia.model.Inscripcion;
import universidad.asistencia.repository.InscripcionRepository;
import universidad.asistencia.util.BusinessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InscripcionService {
    private final InscripcionRepository repository;
    public InscripcionService() { this(new InscripcionRepository()); }
    public InscripcionService(InscripcionRepository repository) { this.repository = repository; }
    public boolean guardar(Inscripcion i) { validar(i); if (repository.existe(i.getEstudiante().getIdEstudiante(), i.getSeccion().getIdSeccion())) throw new BusinessException("El estudiante ya está inscrito en esa sección."); i.setActiva(true); return repository.guardar(i); }
    public List<Inscripcion> listar() { return repository.listar(); }
    public List<Inscripcion> listarPorEstudiante(int idEstudiante) { return repository.listarPorEstudiante(idEstudiante); }
    public List<Inscripcion> listarPorSeccion(int idSeccion) { return repository.listarPorSeccion(idSeccion); }
    public Optional<Inscripcion> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Inscripcion> buscarVigente(int idEstudiante, int idSeccion, LocalDate fecha) { return repository.buscarVigente(idEstudiante, idSeccion, fecha); }
    public boolean actualizar(Inscripcion i) { validar(i); if (i.getIdInscripcion() <= 0) throw new BusinessException("La inscripción no tiene un ID válido."); return repository.actualizar(i); }
    public boolean retirar(int id, LocalDate fecha) { if (fecha == null) throw new BusinessException("La fecha de retiro es obligatoria."); return repository.retirar(id, fecha); }
    private void validar(Inscripcion i) { if (i == null) throw new BusinessException("La inscripción es obligatoria."); if (i.getEstudiante() == null || i.getEstudiante().getIdEstudiante() <= 0) throw new BusinessException("La inscripción debe tener un estudiante válido."); if (i.getSeccion() == null || i.getSeccion().getIdSeccion() <= 0) throw new BusinessException("La inscripción debe tener una sección válida."); if (i.getFechaInscripcion() == null) throw new BusinessException("La fecha de inscripción es obligatoria."); if (i.getFechaRetiro() != null && i.getFechaRetiro().isBefore(i.getFechaInscripcion())) throw new BusinessException("La fecha de retiro no puede ser anterior a la inscripción."); }
}
