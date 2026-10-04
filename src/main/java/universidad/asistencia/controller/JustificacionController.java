package universidad.asistencia.controller;

import universidad.asistencia.enums.EstadoJustificacion;
import universidad.asistencia.model.Justificacion;
import universidad.asistencia.service.JustificacionService;
import java.util.List;
import java.util.Optional;

public class JustificacionController {
    private final JustificacionService service;
    public JustificacionController() { this(new JustificacionService()); }
    public JustificacionController(JustificacionService service) { this.service = service; }
    public boolean registrarJustificacion(Justificacion j) { return service.registrarJustificacion(j); }
    public Optional<Justificacion> buscar(int id) { return service.buscar(id); }
    public List<Justificacion> listar() { return service.listar(); }
    public List<Justificacion> listarPorEstado(EstadoJustificacion estado) { return service.listarPorEstado(estado); }
    public Justificacion aprobarJustificacion(int id, int docente) { return service.aprobarJustificacion(id, docente); }
    public Justificacion rechazarJustificacion(int id, int docente) { return service.rechazarJustificacion(id, docente); }
}
