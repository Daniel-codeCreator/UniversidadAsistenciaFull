package universidad.asistencia.controller;

import universidad.asistencia.model.Seccion;
import universidad.asistencia.service.SeccionService;
import java.util.List;
import java.util.Optional;

public class SeccionController {
    private final SeccionService service;
    public SeccionController() { this(new SeccionService()); }
    public SeccionController(SeccionService service) { this.service = service; }
    public boolean guardar(Seccion s) { return service.guardar(s); }
    public List<Seccion> listar() { return service.listar(); }
    public List<Seccion> listarActivos() { return service.listarActivos(); }
    public Optional<Seccion> buscar(int id) { return service.buscar(id); }
    public Optional<Seccion> buscarPorCodigo(String codigo) { return service.buscarPorCodigo(codigo); }
    public boolean actualizar(Seccion s) { return service.actualizar(s); }
    public boolean desactivar(int id) { return service.desactivar(id); }
}
