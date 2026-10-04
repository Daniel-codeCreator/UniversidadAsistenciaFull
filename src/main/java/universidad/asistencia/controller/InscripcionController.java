package universidad.asistencia.controller;

import universidad.asistencia.model.Inscripcion;
import universidad.asistencia.service.InscripcionService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InscripcionController {
    private final InscripcionService service;
    public InscripcionController() { this(new InscripcionService()); }
    public InscripcionController(InscripcionService service) { this.service = service; }
    public boolean guardar(Inscripcion i) { return service.guardar(i); }
    public List<Inscripcion> listar() { return service.listar(); }
    public List<Inscripcion> listarPorEstudiante(int id) { return service.listarPorEstudiante(id); }
    public List<Inscripcion> listarPorSeccion(int id) { return service.listarPorSeccion(id); }
    public Optional<Inscripcion> buscar(int id) { return service.buscar(id); }
    public Optional<Inscripcion> buscarVigente(int estudiante, int seccion, LocalDate fecha) { return service.buscarVigente(estudiante, seccion, fecha); }
    public boolean actualizar(Inscripcion i) { return service.actualizar(i); }
    public boolean retirar(int id, LocalDate fecha) { return service.retirar(id, fecha); }
}
