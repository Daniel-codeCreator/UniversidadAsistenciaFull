package universidad.asistencia.service;

import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.model.Seccion;
import universidad.asistencia.model.SesionClase;
import universidad.asistencia.repository.SesionClaseRepository;
import universidad.asistencia.util.BusinessException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SesionClaseService {
    private final SesionClaseRepository repository;
    public SesionClaseService() { this(new SesionClaseRepository()); }
    public SesionClaseService(SesionClaseRepository repository) { this.repository = repository; }
    public boolean guardar(SesionClase s) { validar(s); if (repository.buscarPorSeccionFechaHora(s.getSeccion().getIdSeccion(), s.getFecha(), s.getHoraInicioProgramada()).isPresent()) throw new BusinessException("La sesión ya existe para esa fecha y hora."); return repository.guardar(s); }
    public List<SesionClase> listar() { return repository.listar(); }
    public List<SesionClase> listarPorSeccion(int idSeccion) { return repository.listarPorSeccion(idSeccion); }
    public Optional<SesionClase> buscar(int id) { return repository.buscarPorId(id); }
    public boolean actualizar(SesionClase s) { validar(s); if (s.getIdSesion() <= 0) throw new BusinessException("La sesión no tiene un ID válido."); return repository.actualizar(s); }
    public boolean cambiarEstado(int id, EstadoSesion estado) { if (estado == null) throw new BusinessException("El estado es obligatorio."); return repository.actualizarEstado(id, estado); }

    /** Genera una sesión por cada ocurrencia del día indicado dentro del periodo. */
    public List<SesionClase> generarSesiones(PeriodoAcademico periodo, Seccion seccion, HorarioSemanal horario) {
        if (periodo == null || seccion == null || horario == null) throw new BusinessException("Periodo, sección y horario son obligatorios.");
        if (periodo.getFechaInicio() == null || periodo.getFechaFin() == null) throw new BusinessException("El periodo debe tener fechas.");
        List<SesionClase> creadas = new ArrayList<>();
        for (LocalDate fecha = periodo.getFechaInicio(); !fecha.isAfter(periodo.getFechaFin()); fecha = fecha.plusDays(1)) {
            if (fecha.getDayOfWeek() != horario.getDiaSemana()) continue;
            if (repository.buscarPorSeccionFechaHora(seccion.getIdSeccion(), fecha, horario.getHoraInicio()).isPresent()) continue;
            SesionClase sesion = new SesionClase(seccion, fecha, horario.getHoraInicio(), horario.getHoraFin(), seccion.getAulaAsignada(), EstadoSesion.PROGRAMADA);
            if (repository.guardar(sesion)) creadas.add(sesion);
        }
        return creadas;
    }
    public List<SesionClase> generarSesiones(Seccion seccion, HorarioSemanal horario) { if (seccion == null) throw new BusinessException("La sección es obligatoria."); return generarSesiones(seccion.getPeriodoAcademico(), seccion, horario); }
    private void validar(SesionClase s) { if (s == null) throw new BusinessException("La sesión es obligatoria."); if (s.getSeccion() == null || s.getSeccion().getIdSeccion() <= 0) throw new BusinessException("La sesión debe tener una sección válida."); if (s.getFecha() == null || s.getHoraInicioProgramada() == null || s.getHoraFinProgramada() == null) throw new BusinessException("La sesión debe tener fecha y horario."); if (!s.getHoraFinProgramada().isAfter(s.getHoraInicioProgramada())) throw new BusinessException("La hora final debe ser posterior a la inicial."); if (s.getAula() == null || s.getAula().isBlank()) throw new BusinessException("El aula de la sesión es obligatoria."); if (s.getEstado() == null) s.setEstado(EstadoSesion.PROGRAMADA); }
}
