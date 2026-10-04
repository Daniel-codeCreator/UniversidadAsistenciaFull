package universidad.asistencia.controller;

import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.service.HorarioSemanalService;
import java.util.List;
import java.util.Optional;

public class HorarioSemanalController {
    private final HorarioSemanalService service;
    public HorarioSemanalController() { this(new HorarioSemanalService()); }
    public HorarioSemanalController(HorarioSemanalService service) { this.service = service; }
    public boolean guardar(HorarioSemanal h) { return service.guardar(h); }
    public List<HorarioSemanal> listar() { return service.listar(); }
    public List<HorarioSemanal> listarPorSeccion(int idSeccion) { return service.listarPorSeccion(idSeccion); }
    public Optional<HorarioSemanal> buscar(int id) { return service.buscar(id); }
    public boolean actualizar(HorarioSemanal h) { return service.actualizar(h); }
}
