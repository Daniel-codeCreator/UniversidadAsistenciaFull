package universidad.asistencia.service;

import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.repository.HorarioSemanalRepository;
import universidad.asistencia.util.BusinessException;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public class HorarioSemanalService {
    private final HorarioSemanalRepository repository;
    public HorarioSemanalService() { this(new HorarioSemanalRepository()); }
    public HorarioSemanalService(HorarioSemanalRepository repository) { this.repository = repository; }
    public boolean guardar(HorarioSemanal h) { validar(h); if (repository.existe(h.getSeccion().getIdSeccion(), h.getDiaSemana(), h.getHoraInicio())) throw new BusinessException("El horario ya existe para esa sección."); return repository.guardar(h); }
    public List<HorarioSemanal> listar() { return repository.listar(); }
    public List<HorarioSemanal> listarPorSeccion(int idSeccion) { return repository.listarPorSeccion(idSeccion); }
    public Optional<HorarioSemanal> buscar(int id) { return repository.buscarPorId(id); }
    public boolean actualizar(HorarioSemanal h) { validar(h); if (h.getIdHorario() <= 0) throw new BusinessException("El horario no tiene un ID válido."); return repository.actualizar(h); }
    private void validar(HorarioSemanal h) { if (h == null) throw new BusinessException("El horario es obligatorio."); if (h.getSeccion() == null || h.getSeccion().getIdSeccion() <= 0) throw new BusinessException("El horario debe tener una sección válida."); if (h.getDiaSemana() == null) throw new BusinessException("El día de la semana es obligatorio."); if (h.getHoraInicio() == null || h.getHoraFin() == null) throw new BusinessException("El horario debe tener horas."); if (!h.getHoraFin().isAfter(h.getHoraInicio())) throw new BusinessException("La hora final debe ser posterior a la inicial."); }
}
