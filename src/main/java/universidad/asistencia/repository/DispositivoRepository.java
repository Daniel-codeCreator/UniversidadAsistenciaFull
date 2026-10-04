package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.Dispositivo;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DispositivoRepository {
    private static final String COLUMNAS = "id_dispositivo, codigo, nombre, tipo, ubicacion, activo";

    public boolean guardar(Dispositivo dispositivo) {
        String sql = "INSERT INTO Dispositivo (codigo, nombre, tipo, ubicacion, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, dispositivo.getCodigo()); s.setString(2, dispositivo.getNombre()); s.setString(3, dispositivo.getTipo()); s.setString(4, dispositivo.getUbicacion()); s.setBoolean(5, dispositivo.isActivo());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; dispositivo.setIdDispositivo(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el dispositivo", e); }
    }
    public List<Dispositivo> listar() { return consultar("SELECT " + COLUMNAS + " FROM Dispositivo ORDER BY codigo"); }
    public List<Dispositivo> listarActivos() { return consultar("SELECT " + COLUMNAS + " FROM Dispositivo WHERE activo = 1 ORDER BY codigo"); }
    public Optional<Dispositivo> buscarPorId(int id) { return buscar("SELECT " + COLUMNAS + " FROM Dispositivo WHERE id_dispositivo = ?", id); }
    public Optional<Dispositivo> buscarPorCodigo(String codigo) { return buscar("SELECT " + COLUMNAS + " FROM Dispositivo WHERE codigo = ?", codigo); }
    public boolean actualizar(Dispositivo d) {
        String sql = "UPDATE Dispositivo SET codigo = ?, nombre = ?, tipo = ?, ubicacion = ?, activo = ? WHERE id_dispositivo = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setString(1, d.getCodigo()); s.setString(2, d.getNombre()); s.setString(3, d.getTipo()); s.setString(4, d.getUbicacion()); s.setBoolean(5, d.isActivo()); s.setInt(6, d.getIdDispositivo()); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("actualizar el dispositivo", e); }
    }
    public boolean desactivar(int id) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Dispositivo SET activo = 0 WHERE id_dispositivo = ?")) { s.setInt(1, id); return s.executeUpdate() > 0; } catch (SQLException e) { throw JdbcSupport.error("desactivar el dispositivo", e); } }
    public boolean existeCodigo(String codigo) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT 1 FROM Dispositivo WHERE codigo = ?")) { s.setString(1, codigo); try (ResultSet rs = s.executeQuery()) { return rs.next(); } } catch (SQLException e) { throw JdbcSupport.error("verificar el dispositivo", e); } }
    public boolean existeCodigo(String codigo, int idExcluido) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT 1 FROM Dispositivo WHERE codigo = ? AND id_dispositivo <> ?")) { s.setString(1, codigo); s.setInt(2, idExcluido); try (ResultSet rs = s.executeQuery()) { return rs.next(); } } catch (SQLException e) { throw JdbcSupport.error("verificar el dispositivo", e); } }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Dispositivo"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar dispositivos", e); } }
    private List<Dispositivo> consultar(String sql) { List<Dispositivo> r = new ArrayList<>(); try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet rs = s.executeQuery()) { while (rs.next()) r.add(mapear(rs)); return r; } catch (SQLException e) { throw JdbcSupport.error("consultar dispositivos", e); } }
    private Optional<Dispositivo> buscar(String sql, int idOrString) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, idOrString); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } } catch (SQLException e) { throw JdbcSupport.error("buscar el dispositivo", e); } }
    private Optional<Dispositivo> buscar(String sql, String codigo) { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setString(1, codigo); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } } catch (SQLException e) { throw JdbcSupport.error("buscar el dispositivo", e); } }
    private Dispositivo mapear(ResultSet rs) throws SQLException { return new Dispositivo(rs.getInt("id_dispositivo"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("tipo"), rs.getString("ubicacion"), rs.getBoolean("activo")); }
}
