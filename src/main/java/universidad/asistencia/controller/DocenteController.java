package universidad.asistencia.controller;

import universidad.asistencia.model.Docente;
import universidad.asistencia.service.DocenteService;
import java.util.List;
import java.util.Optional;

public class DocenteController {
    private final DocenteService service;
    public DocenteController() { this(new DocenteService()); }
    public DocenteController(DocenteService service) { this.service = service; }
    public boolean guardar(Docente d) { return service.guardar(d); }
    public List<Docente> listar() { return service.listar(); }
    public List<Docente> listarActivos() { return service.listarActivos(); }
    public Optional<Docente> buscar(int id) { return service.buscar(id); }
    public Optional<Docente> buscarPorCodigo(String codigo) { return service.buscarPorCodigo(codigo); }
    public boolean actualizar(Docente d) { return service.actualizar(d); }
    public boolean desactivar(int id) { return service.desactivar(id); }
}
