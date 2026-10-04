package universidad.asistencia.controller;

import universidad.asistencia.model.Dispositivo;
import universidad.asistencia.service.DispositivoService;
import java.util.List;
import java.util.Optional;

public class DispositivoController {
    private final DispositivoService service;
    public DispositivoController() { this(new DispositivoService()); }
    public DispositivoController(DispositivoService service) { this.service = service; }
    public boolean guardar(Dispositivo d) { return service.guardar(d); }
    public List<Dispositivo> listar() { return service.listar(); }
    public List<Dispositivo> listarActivos() { return service.listarActivos(); }
    public Optional<Dispositivo> buscar(int id) { return service.buscar(id); }
    public Optional<Dispositivo> buscarPorCodigo(String codigo) { return service.buscarPorCodigo(codigo); }
    public boolean actualizar(Dispositivo d) { return service.actualizar(d); }
    public boolean desactivar(int id) { return service.desactivar(id); }
}
