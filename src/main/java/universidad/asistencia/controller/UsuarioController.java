package universidad.asistencia.controller;

import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Usuario;
import universidad.asistencia.service.UsuarioService;
import java.util.List;
import java.util.Optional;

public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController() { this(new UsuarioService()); }
    public UsuarioController(UsuarioService service) { this.service = service; }
    public Usuario crearUsuario(String usuario, String password, RolUsuario rol, Docente docente) { return service.crearUsuario(usuario, password, rol, docente); }
    public Optional<Usuario> autenticar(String usuario, String password) { return service.autenticar(usuario, password); }
    public Optional<Usuario> buscar(int id) { return service.buscar(id); }
    public Optional<Usuario> buscarPorUsuario(String usuario) { return service.buscarPorUsuario(usuario); }
    public List<Usuario> listar() { return service.listar(); }
    public boolean actualizar(Usuario usuario) { return service.actualizar(usuario); }
    public boolean actualizarConPassword(Usuario usuario, String password) { return service.actualizarConPassword(usuario, password); }
    public boolean desactivar(int id) { return service.desactivar(id); }
}
