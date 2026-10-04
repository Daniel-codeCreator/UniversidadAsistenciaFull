package universidad.asistencia.service;

import universidad.asistencia.model.Curso;
import universidad.asistencia.repository.CursoRepository;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CursoService {
    private final CursoRepository repository;
    public CursoService() { this(new CursoRepository()); }
    public CursoService(CursoRepository repository) { this.repository = repository; }
    public boolean guardar(Curso c) { validar(c); if (repository.existeCodigo(c.getCodigo())) throw new BusinessException("El código del curso ya está registrado."); c.setActivo(true); return repository.guardar(c); }
    public List<Curso> listar() { return repository.listar(); }
    public List<Curso> listarActivos() { return repository.listarActivos(); }
    public Optional<Curso> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Curso> buscarPorCodigo(String codigo) { return repository.buscarPorCodigo(codigo); }
    public boolean actualizar(Curso c) { validar(c); if (c.getIdCurso() <= 0) throw new BusinessException("El curso no tiene un ID válido."); if (repository.existeCodigo(c.getCodigo(), c.getIdCurso())) throw new BusinessException("El código del curso ya está registrado."); return repository.actualizar(c); }
    public boolean desactivar(int id) { return repository.desactivar(id); }
    private void validar(Curso c) { Objects.requireNonNull(c, "El curso es obligatorio."); requerido(c.getCodigo(), "El código del curso es obligatorio."); requerido(c.getNombre(), "El nombre del curso es obligatorio."); if (c.getCreditos() <= 0) throw new BusinessException("Los créditos deben ser mayores que cero."); }
    private void requerido(String s, String m) { if (s == null || s.isBlank()) throw new BusinessException(m); }
}
