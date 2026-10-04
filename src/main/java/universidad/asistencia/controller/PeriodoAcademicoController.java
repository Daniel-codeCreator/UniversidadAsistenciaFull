package universidad.asistencia.controller;

import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.service.PeriodoAcademicoService;
import java.util.List;
import java.util.Optional;

public class PeriodoAcademicoController {
    private final PeriodoAcademicoService service;
    public PeriodoAcademicoController() { this(new PeriodoAcademicoService()); }
    public PeriodoAcademicoController(PeriodoAcademicoService service) { this.service = service; }
    public boolean guardar(PeriodoAcademico p) { return service.guardar(p); }
    public List<PeriodoAcademico> listar() { return service.listar(); }
    public List<PeriodoAcademico> listarActivos() { return service.listarActivos(); }
    public Optional<PeriodoAcademico> buscar(int id) { return service.buscar(id); }
    public Optional<PeriodoAcademico> buscarPorNombre(String nombre) { return service.buscarPorNombre(nombre); }
    public boolean actualizar(PeriodoAcademico p) { return service.actualizar(p); }
    public boolean desactivar(int id) { return service.desactivar(id); }
}
