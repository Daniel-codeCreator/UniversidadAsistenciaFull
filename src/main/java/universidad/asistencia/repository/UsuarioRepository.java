package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Usuario;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    private static final String SELECT = "SELECT u.id_usuario, u.usuario, u.password_hash, u.rol, u.activo, u.fecha_creacion, " +
            "d.id_docente, d.codigo_empleado, d.nombres AS docente_nombres, d.apellidos AS docente_apellidos, d.correo AS docente_correo, d.activo AS docente_activo " +
            "FROM Usuario u LEFT JOIN Docente d ON d.id_docente = u.id_docente ";

    public boolean guardar(Usuario usuario) {
        String sql = "INSERT INTO Usuario (usuario, password_hash, rol, id_docente, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, usuario.getUsuario()); s.setString(2, usuario.getPasswordHash()); s.setString(3, usuario.getRol().name());
            if (usuario.getDocente() == null) s.setNull(4, Types.INTEGER); else s.setInt(4, usuario.getDocente().getIdDocente()); s.setBoolean(5, usuario.isActivo());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; usuario.setIdUsuario(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el usuario", e); }
    }
    public Optional<Usuario> buscarPorId(int id) { return buscar(SELECT + "WHERE u.id_usuario = ?", id); }
    public Optional<Usuario> buscarPorUsuario(String nombre) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(SELECT + "WHERE u.usuario = ?")) { s.setString(1, nombre); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar el usuario", e); }
    }
    public List<Usuario> listar() { List<Usuario> r = new ArrayList<>(); try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(SELECT + "ORDER BY u.usuario"); ResultSet rs = s.executeQuery()) { while (rs.next()) r.add(mapear(rs)); return r; } catch (SQLException e) { throw JdbcSupport.error("consultar usuarios", e); } }
    public boolean existeUsuario(String nombre) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT 1 FROM Usuario WHERE usuario = ?")) { s.setString(1, nombre); try (ResultSet rs = s.executeQuery()) { return rs.next(); } } catch (SQLException e) { throw JdbcSupport.error("verificar el usuario", e); } }
    public boolean existeUsuario(String nombre, int idExcluido) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT 1 FROM Usuario WHERE usuario = ? AND id_usuario <> ?")) { s.setString(1, nombre); s.setInt(2, idExcluido); try (ResultSet rs = s.executeQuery()) { return rs.next(); } } catch (SQLException e) { throw JdbcSupport.error("verificar el usuario", e); } }
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE Usuario SET usuario = ?, password_hash = ?, rol = ?, id_docente = ?, activo = ? WHERE id_usuario = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setString(1, u.getUsuario()); s.setString(2, u.getPasswordHash()); s.setString(3, u.getRol().name()); if (u.getDocente() == null) s.setNull(4, Types.INTEGER); else s.setInt(4, u.getDocente().getIdDocente()); s.setBoolean(5, u.isActivo()); s.setInt(6, u.getIdUsuario()); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("actualizar el usuario", e); }
    }
    public boolean desactivar(int id) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Usuario SET activo = 0 WHERE id_usuario = ?")) { s.setInt(1, id); return s.executeUpdate() > 0; } catch (SQLException e) { throw JdbcSupport.error("desactivar el usuario", e); } }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Usuario"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar usuarios", e); } }
    private Optional<Usuario> buscar(String sql, int id) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } } catch (SQLException e) { throw JdbcSupport.error("buscar el usuario", e); } }
    private Usuario mapear(ResultSet rs) throws SQLException {
        Docente docente = null;
        if (rs.getObject("id_docente") != null) docente = new Docente(rs.getInt("id_docente"), rs.getString("codigo_empleado"), rs.getString("docente_nombres"), rs.getString("docente_apellidos"), rs.getString("docente_correo"), rs.getBoolean("docente_activo"));
        return new Usuario(rs.getInt("id_usuario"), rs.getString("usuario"), rs.getString("password_hash"), RolUsuario.valueOf(rs.getString("rol")), docente, rs.getBoolean("activo"), rs.getObject("fecha_creacion", LocalDateTime.class));
    }
}
