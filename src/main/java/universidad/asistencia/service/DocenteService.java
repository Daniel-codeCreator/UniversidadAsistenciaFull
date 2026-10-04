package universidad.asistencia.service;

import universidad.asistencia.model.Docente;
import universidad.asistencia.repository.DocenteRepository;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DocenteService {
    private final DocenteRepository repository;
    public DocenteService() { this(new DocenteRepository()); }
    public DocenteService(DocenteRepository repository) { this.repository = repository; }
    public boolean guardar(Docente d) { validar(d); if (repository.existeCodigo(d.getCodigoEmpleado())) throw new BusinessException("El código de empleado ya está registrado."); if (repository.existeCorreo(d.getCorreo())) throw new BusinessException("El correo ya está registrado."); d.setActivo(true); return repository.guardar(d); }
    public List<Docente> listar() { return repository.listar(); }
    public List<Docente> listarActivos() { return repository.listarActivos(); }
    public Optional<Docente> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Docente> buscarPorCodigo(String codigo) { return repository.buscarPorCodigo(codigo); }
    public boolean actualizar(Docente d) { validar(d); if (d.getIdDocente() <= 0) throw new BusinessException("El docente no tiene un ID válido."); if (repository.existeCodigo(d.getCodigoEmpleado(), d.getIdDocente())) throw new BusinessException("El código de empleado ya está registrado."); if (repository.existeCorreo(d.getCorreo(), d.getIdDocente())) throw new BusinessException("El correo ya está registrado."); return repository.actualizar(d); }
    public boolean desactivar(int id) { return repository.desactivar(id); }
    private void validar(Docente d) { Objects.requireNonNull(d, "El docente es obligatorio."); requerido(d.getCodigoEmpleado(), "El código de empleado es obligatorio."); requerido(d.getNombres(), "Los nombres son obligatorios."); requerido(d.getApellidos(), "Los apellidos son obligatorios."); requerido(d.getCorreo(), "El correo es obligatorio."); }
    private void requerido(String s, String m) { if (s == null || s.isBlank()) throw new BusinessException(m); }
}
