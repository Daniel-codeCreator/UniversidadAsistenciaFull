package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.Curso;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.model.Seccion;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeccionRepository {
    private static final String JOIN = " FROM Seccion s " +
            "JOIN Curso c ON c.id_curso = s.id_curso " +
            "JOIN PeriodoAcademico p ON p.id_periodo = s.id_periodo " +
            "JOIN Docente d ON d.id_docente = s.id_docente ";
    private static final String COLUMNAS = "s.id_seccion, s.codigo, s.aula_asignada, s.activo, " +
            "c.id_curso AS curso_id, c.codigo AS curso_codigo, c.nombre AS curso_nombre, c.descripcion AS curso_descripcion, c.creditos AS curso_creditos, c.activo AS curso_activo, " +
            "p.id_periodo AS periodo_id, p.nombre AS periodo_nombre, p.fecha_inicio AS periodo_inicio, p.fecha_fin AS periodo_fin, p.activo AS periodo_activo, " +
            "d.id_docente AS docente_id, d.codigo_empleado AS docente_codigo, d.nombres AS docente_nombres, d.apellidos AS docente_apellidos, d.correo AS docente_correo, d.activo AS docente_activo";

    public boolean guardar(Seccion seccion) {
        String sql = "INSERT INTO Seccion (codigo, id_curso, id_periodo, id_docente, aula_asignada, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, seccion.getCodigo()); s.setInt(2, seccion.getCurso().getIdCurso()); s.setInt(3, seccion.getPeriodoAcademico().getIdPeriodo());
            s.setInt(4, seccion.getDocente().getIdDocente()); s.setString(5, seccion.getAulaAsignada()); s.setBoolean(6, seccion.isActivo());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; seccion.setIdSeccion(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar la sección", e); }
    }

    public List<Seccion> listar() { return consultar("SELECT " + COLUMNAS + JOIN + " ORDER BY c.codigo, s.codigo"); }
    public List<Seccion> listarActivos() { return consultar("SELECT " + COLUMNAS + JOIN + " WHERE s.activo = 1 ORDER BY c.codigo, s.codigo"); }
    public Optional<Seccion> buscarPorId(int id) { return buscar("SELECT " + COLUMNAS + JOIN + " WHERE s.id_seccion = ?", id); }
    public Optional<Seccion> buscarPorCodigo(String codigo) { return buscar("SELECT " + COLUMNAS + JOIN + " WHERE s.codigo = ?", codigo); }
    public Optional<Seccion> buscar(int idCurso, int idPeriodo, String codigo) {
        String sql = "SELECT " + COLUMNAS + JOIN + " WHERE s.id_curso = ? AND s.id_periodo = ? AND s.codigo = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idCurso); s.setInt(2, idPeriodo); s.setString(3, codigo);
            try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar la sección", e); }
    }

    public boolean actualizar(Seccion seccion) {
        String sql = "UPDATE Seccion SET codigo = ?, id_curso = ?, id_periodo = ?, id_docente = ?, aula_asignada = ?, activo = ? WHERE id_seccion = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, seccion.getCodigo()); s.setInt(2, seccion.getCurso().getIdCurso()); s.setInt(3, seccion.getPeriodoAcademico().getIdPeriodo());
            s.setInt(4, seccion.getDocente().getIdDocente()); s.setString(5, seccion.getAulaAsignada()); s.setBoolean(6, seccion.isActivo()); s.setInt(7, seccion.getIdSeccion());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("actualizar la sección", e); }
    }
    public boolean desactivar(int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Seccion SET activo = 0 WHERE id_seccion = ?")) { s.setInt(1, id); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("desactivar la sección", e); }
    }
    public boolean existe(int idCurso, int idPeriodo, String codigo) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT 1 FROM Seccion WHERE id_curso = ? AND id_periodo = ? AND codigo = ?")) {
            s.setInt(1, idCurso); s.setInt(2, idPeriodo); s.setString(3, codigo); try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw JdbcSupport.error("verificar la sección", e); }
    }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Seccion"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar secciones", e); } }

    private List<Seccion> consultar(String sql) {
        List<Seccion> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet rs = s.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); return resultado; }
        catch (SQLException e) { throw JdbcSupport.error("consultar secciones", e); }
    }
    private Optional<Seccion> buscar(String sql, int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar la sección", e); }
    }
    private Optional<Seccion> buscar(String sql, String codigo) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, codigo);
            try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar la sección", e); }
    }
    static Seccion mapear(ResultSet rs) throws SQLException {
        Curso curso = new Curso(rs.getInt("curso_id"), rs.getString("curso_codigo"), rs.getString("curso_nombre"), rs.getString("curso_descripcion"), rs.getInt("curso_creditos"), rs.getBoolean("curso_activo"));
        PeriodoAcademico periodo = new PeriodoAcademico(rs.getInt("periodo_id"), rs.getString("periodo_nombre"), JdbcSupport.localDate(rs, "periodo_inicio"), JdbcSupport.localDate(rs, "periodo_fin"), rs.getBoolean("periodo_activo"));
        Docente docente = new Docente(rs.getInt("docente_id"), rs.getString("docente_codigo"), rs.getString("docente_nombres"), rs.getString("docente_apellidos"), rs.getString("docente_correo"), rs.getBoolean("docente_activo"));
        return new Seccion(rs.getInt("id_seccion"), rs.getString("codigo"), curso, periodo, docente, rs.getString("aula_asignada"), rs.getBoolean("activo"));
    }
}
