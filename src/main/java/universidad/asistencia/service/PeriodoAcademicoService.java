package universidad.asistencia.service;

import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.repository.PeriodoAcademicoRepository;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PeriodoAcademicoService {
    private final PeriodoAcademicoRepository repository;
    public PeriodoAcademicoService() { this(new PeriodoAcademicoRepository()); }
    public PeriodoAcademicoService(PeriodoAcademicoRepository repository) { this.repository = repository; }
    public boolean guardar(PeriodoAcademico p) { validar(p); if (repository.existeNombre(p.getNombre())) throw new BusinessException("El nombre del periodo ya está registrado."); p.setActivo(true); return repository.guardar(p); }
    public List<PeriodoAcademico> listar() { return repository.listar(); }
    public List<PeriodoAcademico> listarActivos() { return repository.listarActivos(); }
    public Optional<PeriodoAcademico> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<PeriodoAcademico> buscarPorNombre(String nombre) { return repository.buscarPorNombre(nombre); }
    public boolean actualizar(PeriodoAcademico p) { validar(p); if (p.getIdPeriodo() <= 0) throw new BusinessException("El periodo no tiene un ID válido."); if (repository.existeNombre(p.getNombre(), p.getIdPeriodo())) throw new BusinessException("El nombre del periodo ya está registrado."); return repository.actualizar(p); }
    public boolean desactivar(int id) { return repository.desactivar(id); }
    private void validar(PeriodoAcademico p) { Objects.requireNonNull(p, "El periodo académico es obligatorio."); requerido(p.getNombre(), "El nombre del periodo es obligatorio."); if (p.getFechaInicio() == null || p.getFechaFin() == null) throw new BusinessException("El periodo debe tener fechas."); if (p.getFechaFin().isBefore(p.getFechaInicio())) throw new BusinessException("La fecha final no puede ser anterior a la inicial."); }
    private void requerido(String s, String m) { if (s == null || s.isBlank()) throw new BusinessException(m); }
}
