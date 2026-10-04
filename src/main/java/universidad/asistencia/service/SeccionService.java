package universidad.asistencia.service;

import universidad.asistencia.model.Seccion;
import universidad.asistencia.repository.SeccionRepository;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SeccionService {
    private final SeccionRepository repository;
    public SeccionService() { this(new SeccionRepository()); }
    public SeccionService(SeccionRepository repository) { this.repository = repository; }
    public boolean guardar(Seccion s) { validar(s); if (repository.existe(s.getCurso().getIdCurso(), s.getPeriodoAcademico().getIdPeriodo(), s.getCodigo())) throw new BusinessException("La sección ya existe para ese curso y periodo."); s.setActivo(true); return repository.guardar(s); }
    public List<Seccion> listar() { return repository.listar(); }
    public List<Seccion> listarActivos() { return repository.listarActivos(); }
    public Optional<Seccion> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Seccion> buscarPorCodigo(String codigo) { return repository.buscarPorCodigo(codigo); }
    public boolean actualizar(Seccion s) { validar(s); if (s.getIdSeccion() <= 0) throw new BusinessException("La sección no tiene un ID válido."); return repository.actualizar(s); }
    public boolean desactivar(int id) { return repository.desactivar(id); }
    private void validar(Seccion s) { Objects.requireNonNull(s, "La sección es obligatoria."); requerido(s.getCodigo(), "El código de la sección es obligatorio."); requerido(s.getAulaAsignada(), "El aula de la sección es obligatoria."); if (s.getCurso() == null || s.getCurso().getIdCurso() <= 0) throw new BusinessException("La sección debe tener un curso válido."); if (s.getPeriodoAcademico() == null || s.getPeriodoAcademico().getIdPeriodo() <= 0) throw new BusinessException("La sección debe tener un periodo válido."); if (s.getDocente() == null || s.getDocente().getIdDocente() <= 0) throw new BusinessException("La sección debe tener un docente válido."); }
    private void requerido(String s, String m) { if (s == null || s.isBlank()) throw new BusinessException(m); }
}
