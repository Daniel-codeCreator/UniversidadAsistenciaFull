package universidad.asistencia.controller;

import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.model.*;
import universidad.asistencia.service.SesionClaseService;
import java.util.List;
import java.util.Optional;

public class SesionClaseController {
    private final SesionClaseService service;
    public SesionClaseController() { this(new SesionClaseService()); }
    public SesionClaseController(SesionClaseService service) { this.service = service; }
    public boolean guardar(SesionClase s) { return service.guardar(s); }
    public List<SesionClase> listar() { return service.listar(); }
    public List<SesionClase> listarPorSeccion(int id) { return service.listarPorSeccion(id); }
    public Optional<SesionClase> buscar(int id) { return service.buscar(id); }
    public boolean actualizar(SesionClase s) { return service.actualizar(s); }
    public boolean cambiarEstado(int id, EstadoSesion estado) { return service.cambiarEstado(id, estado); }
    public List<SesionClase> generarSesiones(PeriodoAcademico p, Seccion s, HorarioSemanal h) { return service.generarSesiones(p, s, h); }
}
