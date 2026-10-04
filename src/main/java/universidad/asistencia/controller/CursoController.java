package universidad.asistencia.controller;

import universidad.asistencia.model.Curso;
import universidad.asistencia.service.CursoService;
import java.util.List;
import java.util.Optional;

public class CursoController {
    private final CursoService service;
    public CursoController() { this(new CursoService()); }
    public CursoController(CursoService service) { this.service = service; }
    public boolean guardar(Curso c) { return service.guardar(c); }
    public List<Curso> listar() { return service.listar(); }
    public List<Curso> listarActivos() { return service.listarActivos(); }
    public Optional<Curso> buscar(int id) { return service.buscar(id); }
    public Optional<Curso> buscarPorCodigo(String codigo) { return service.buscarPorCodigo(codigo); }
    public boolean actualizar(Curso c) { return service.actualizar(c); }
    public boolean desactivar(int id) { return service.desactivar(id); }
}
