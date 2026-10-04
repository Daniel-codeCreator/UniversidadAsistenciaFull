package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PeriodoAcademicoRepository {
    private static final String COLUMNAS = "id_periodo, nombre, fecha_inicio, fecha_fin, activo";

    public boolean guardar(PeriodoAcademico periodo) {
        String sql = "INSERT INTO PeriodoAcademico (nombre, fecha_inicio, fecha_fin, activo) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, periodo.getNombre()); s.setObject(2, periodo.getFechaInicio()); s.setObject(3, periodo.getFechaFin()); s.setBoolean(4, periodo.isActivo());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; periodo.setIdPeriodo(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el periodo académico", e); }
    }

    public List<PeriodoAcademico> listar() { return consultar("SELECT " + COLUMNAS + " FROM PeriodoAcademico ORDER BY fecha_inicio"); }
    public List<PeriodoAcademico> listarActivos() { return consultar("SELECT " + COLUMNAS + " FROM PeriodoAcademico WHERE activo = 1 ORDER BY fecha_inicio"); }
    public Optional<PeriodoAcademico> buscarPorId(int id) { return buscar("SELECT " + COLUMNAS + " FROM PeriodoAcademico WHERE id_periodo = ?", id); }
    public Optional<PeriodoAcademico> buscarPorNombre(String nombre) { return buscar("SELECT " + COLUMNAS + " FROM PeriodoAcademico WHERE nombre = ?", nombre); }

    public boolean actualizar(PeriodoAcademico periodo) {
        String sql = "UPDATE PeriodoAcademico SET nombre = ?, fecha_inicio = ?, fecha_fin = ?, activo = ? WHERE id_periodo = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, periodo.getNombre()); s.setObject(2, periodo.getFechaInicio()); s.setObject(3, periodo.getFechaFin()); s.setBoolean(4, periodo.isActivo()); s.setInt(5, periodo.getIdPeriodo()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("actualizar el periodo académico", e); }
    }
    public boolean desactivar(int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE PeriodoAcademico SET activo = 0 WHERE id_periodo = ?")) { s.setInt(1, id); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("desactivar el periodo académico", e); }
    }
    public boolean existeNombre(String nombre) { return existe("SELECT 1 FROM PeriodoAcademico WHERE nombre = ?", nombre); }
    public boolean existeNombre(String nombre, int idExcluido) { return existe("SELECT 1 FROM PeriodoAcademico WHERE nombre = ? AND id_periodo <> ?", nombre, idExcluido); }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM PeriodoAcademico"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar periodos académicos", e); } }

    private List<PeriodoAcademico> consultar(String sql) {
        List<PeriodoAcademico> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet rs = s.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); return resultado; }
        catch (SQLException e) { throw JdbcSupport.error("consultar periodos académicos", e); }
    }
    private Optional<PeriodoAcademico> buscar(String sql, Object valor) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            if (valor instanceof Integer id) s.setInt(1, id); else s.setString(1, (String) valor);
            try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar el periodo académico", e); }
    }
    private boolean existe(String sql, Object... values) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) { if (values[i] instanceof Integer id) s.setInt(i + 1, id); else s.setString(i + 1, (String) values[i]); }
            try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw JdbcSupport.error("verificar el periodo académico", e); }
    }
    private PeriodoAcademico mapear(ResultSet rs) throws SQLException {
        return new PeriodoAcademico(rs.getInt("id_periodo"), rs.getString("nombre"), JdbcSupport.localDate(rs, "fecha_inicio"), JdbcSupport.localDate(rs, "fecha_fin"), rs.getBoolean("activo"));
    }
}
