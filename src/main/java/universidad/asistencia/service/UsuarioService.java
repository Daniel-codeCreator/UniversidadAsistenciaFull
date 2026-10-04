package universidad.asistencia.service;

import org.mindrot.jbcrypt.BCrypt;
import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.model.Usuario;
import universidad.asistencia.model.Docente;
import universidad.asistencia.repository.UsuarioRepository;
import universidad.asistencia.util.BusinessException;

import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private final UsuarioRepository repository;
    public UsuarioService() { this(new UsuarioRepository()); }
    public UsuarioService(UsuarioRepository repository) { this.repository = repository; }

    public Usuario crearUsuario(String usuario, String password, RolUsuario rol, Docente docente) {
        if (usuario == null || usuario.isBlank()) throw new BusinessException("El usuario es obligatorio.");
        if (password == null || password.isBlank()) throw new BusinessException("La contraseña es obligatoria.");
        if (rol == null) throw new BusinessException("El rol es obligatorio.");
        if (rol == RolUsuario.DOCENTE && (docente == null || docente.getIdDocente() <= 0)) throw new BusinessException("Un usuario DOCENTE debe tener docente relacionado.");
        if (rol == RolUsuario.ADMIN && docente != null) throw new BusinessException("Un usuario ADMIN no debe tener docente relacionado.");
        if (repository.existeUsuario(usuario)) throw new BusinessException("El nombre de usuario ya está registrado.");
        Usuario nuevo = new Usuario(usuario, BCrypt.hashpw(password, BCrypt.gensalt()), rol, docente);
        repository.guardar(nuevo);
        return nuevo;
    }

    /** Guarda un usuario que ya trae un hash BCrypt; no acepta contraseñas planas. */
    public boolean guardar(Usuario usuario) {
        validarUsuario(usuario);
        if (!esHashBcrypt(usuario.getPasswordHash())) throw new BusinessException("passwordHash debe ser un hash BCrypt.");
        if (repository.existeUsuario(usuario.getUsuario())) throw new BusinessException("El nombre de usuario ya está registrado.");
        return repository.guardar(usuario);
    }
    public Optional<Usuario> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Usuario> buscarPorUsuario(String nombre) { return repository.buscarPorUsuario(nombre); }
    public List<Usuario> listar() { return repository.listar(); }
    public boolean actualizar(Usuario usuario) {
        validarUsuario(usuario);
        if (usuario.getIdUsuario() <= 0) throw new BusinessException("El usuario no tiene un ID válido.");
        if (!esHashBcrypt(usuario.getPasswordHash())) throw new BusinessException("passwordHash debe ser un hash BCrypt.");
        if (repository.existeUsuario(usuario.getUsuario(), usuario.getIdUsuario())) throw new BusinessException("El nombre de usuario ya está registrado.");
        return repository.actualizar(usuario);
    }
    /** Actualiza un usuario y deja el hash de la nueva contraseña en esta capa. */
    public boolean actualizarConPassword(Usuario usuario, String password) {
        if (password == null || password.isBlank()) throw new BusinessException("La contraseña es obligatoria.");
        validarUsuario(usuario);
        if (usuario.getIdUsuario() <= 0) throw new BusinessException("El usuario no tiene un ID válido.");
        if (repository.existeUsuario(usuario.getUsuario(), usuario.getIdUsuario())) throw new BusinessException("El nombre de usuario ya está registrado.");
        usuario.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        return repository.actualizar(usuario);
    }
    public boolean desactivar(int id) { return repository.desactivar(id); }
    public Optional<Usuario> autenticar(String usuario, String password) {
        if (usuario == null || password == null) return Optional.empty();
        Optional<Usuario> encontrado = repository.buscarPorUsuario(usuario);
        if (encontrado.isEmpty() || !encontrado.get().isActivo()) return Optional.empty();
        try { return BCrypt.checkpw(password, encontrado.get().getPasswordHash()) ? encontrado : Optional.empty(); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }
    private void validarUsuario(Usuario u) { if (u == null || u.getUsuario() == null || u.getUsuario().isBlank()) throw new BusinessException("El usuario es obligatorio."); if (u.getRol() == null) throw new BusinessException("El rol es obligatorio."); if (u.getRol() == RolUsuario.DOCENTE && (u.getDocente() == null || u.getDocente().getIdDocente() <= 0)) throw new BusinessException("Un usuario DOCENTE debe tener docente relacionado."); if (u.getRol() == RolUsuario.ADMIN && u.getDocente() != null) throw new BusinessException("Un usuario ADMIN no debe tener docente relacionado."); }
    private boolean esHashBcrypt(String hash) { return hash != null && hash.matches("\\A\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}\\z"); }
}
