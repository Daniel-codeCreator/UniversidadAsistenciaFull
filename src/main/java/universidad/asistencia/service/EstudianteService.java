package universidad.asistencia.service;

import universidad.asistencia.model.Estudiante;
import universidad.asistencia.repository.EstudianteRepository;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EstudianteService {
    private final EstudianteRepository repository;
    public EstudianteService() { this(new EstudianteRepository()); }
    public EstudianteService(EstudianteRepository repository) { this.repository = repository; }

    public boolean guardar(Estudiante estudiante) {
        validar(estudiante);
        if (repository.existeCarnet(estudiante.getCarnet())) throw new BusinessException("El carnet ya está registrado.");
        if (repository.existeCorreo(estudiante.getCorreo())) throw new BusinessException("El correo ya está registrado.");
        estudiante.setActivo(true);
        return repository.guardar(estudiante);
    }
    public List<Estudiante> listar() { return repository.listar(); }
    public List<Estudiante> listarActivos() { return repository.listarActivos(); }
    public Optional<Estudiante> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Estudiante> buscarPorId(int id) { return buscar(id); }
    public Optional<Estudiante> buscarPorCarnet(String carnet) { return repository.buscarPorCarnet(carnet); }
    public boolean actualizar(Estudiante estudiante) {
        validar(estudiante);
        if (estudiante.getIdEstudiante() <= 0) throw new BusinessException("El estudiante no tiene un ID válido.");
        if (repository.existeCarnet(estudiante.getCarnet(), estudiante.getIdEstudiante())) throw new BusinessException("El carnet ya está registrado.");
        if (repository.existeCorreo(estudiante.getCorreo(), estudiante.getIdEstudiante())) throw new BusinessException("El correo ya está registrado.");
        return repository.actualizar(estudiante);
    }
    public boolean desactivar(int id) { return repository.desactivar(id); }

    private void validar(Estudiante e) {
        Objects.requireNonNull(e, "El estudiante es obligatorio.");
        requerido(e.getCarnet(), "El carnet es obligatorio.");
        requerido(e.getNombres(), "Los nombres son obligatorios.");
        requerido(e.getApellidos(), "Los apellidos son obligatorios.");
        requerido(e.getCorreo(), "El correo es obligatorio.");
    }
    private void requerido(String value, String message) { if (value == null || value.isBlank()) throw new BusinessException(message); }
}
