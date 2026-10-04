package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.*;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InscripcionRepository {
    private static final String SELECT = "SELECT i.id_inscripcion, i.fecha_inscripcion, i.fecha_retiro, i.activa, " +
            "e.id_estudiante, e.carnet, e.nombres AS estudiante_nombres, e.apellidos AS estudiante_apellidos, e.correo AS estudiante_correo, e.activo AS estudiante_activo, " +
            "s.id_seccion, s.codigo AS seccion_codigo, s.aula_asignada, s.activo AS seccion_activo, " +
            "c.id_curso AS curso_id, c.codigo AS curso_codigo, c.nombre AS curso_nombre, c.descripcion AS curso_descripcion, c.creditos AS curso_creditos, c.activo AS curso_activo, " +
            "p.id_periodo AS periodo_id, p.nombre AS periodo_nombre, p.fecha_inicio AS periodo_inicio, p.fecha_fin AS periodo_fin, p.activo AS periodo_activo, " +
            "d.id_docente AS docente_id, d.codigo_empleado AS docente_codigo, d.nombres AS docente_nombres, d.apellidos AS docente_apellidos, d.correo AS docente_correo, d.activo AS docente_activo " +
            "FROM Inscripcion i JOIN Estudiante e ON e.id_estudiante = i.id_estudiante " +
            "JOIN Seccion s ON s.id_seccion = i.id_seccion JOIN Curso c ON c.id_curso = s.id_curso " +
            "JOIN PeriodoAcademico p ON p.id_periodo = s.id_periodo JOIN Docente d ON d.id_docente = s.id_docente ";

    public boolean guardar(Inscripcion inscripcion) {
        String sql = "INSERT INTO Inscripcion (id_estudiante, id_seccion, fecha_inscripcion, fecha_retiro, activa) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, inscripcion.getEstudiante().getIdEstudiante()); s.setInt(2, inscripcion.getSeccion().getIdSeccion());
            s.setObject(3, inscripcion.getFechaInscripcion());
            if (inscripcion.getFechaRetiro() == null) s.setNull(4, Types.DATE); else s.setObject(4, inscripcion.getFechaRetiro());
            s.setBoolean(5, inscripcion.isActiva());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; inscripcion.setIdInscripcion(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar la inscripción", e); }
    }
    public Optional<Inscripcion> buscarPorId(int id) { return buscar(SELECT + "WHERE i.id_inscripcion = ?", id); }
    public List<Inscripcion> listar() { return consultar(SELECT + "ORDER BY i.fecha_inscripcion, e.apellidos"); }
    public List<Inscripcion> listarPorEstudiante(int idEstudiante) { return consultar(SELECT + "WHERE i.id_estudiante = ?", idEstudiante); }
    public List<Inscripcion> listarPorSeccion(int idSeccion) { return consultar(SELECT + "WHERE i.id_seccion = ?", idSeccion); }

    public Optional<Inscripcion> buscarVigente(int idEstudiante, int idSeccion, LocalDate fechaSesion) {
        String sql = SELECT + "WHERE i.id_estudiante = ? AND i.id_seccion = ? AND i.activa = 1 " +
                "AND i.fecha_inscripcion <= ? AND (i.fecha_retiro IS NULL OR i.fecha_retiro >= ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idEstudiante); s.setInt(2, idSeccion); s.setObject(3, fechaSesion); s.setObject(4, fechaSesion);
            try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar la inscripción vigente", e); }
    }
    public boolean existe(int idEstudiante, int idSeccion) { return existe("SELECT 1 FROM Inscripcion WHERE id_estudiante = ? AND id_seccion = ?", idEstudiante, idSeccion); }

    public boolean actualizar(Inscripcion inscripcion) {
        String sql = "UPDATE Inscripcion SET id_estudiante = ?, id_seccion = ?, fecha_inscripcion = ?, fecha_retiro = ?, activa = ? WHERE id_inscripcion = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, inscripcion.getEstudiante().getIdEstudiante()); s.setInt(2, inscripcion.getSeccion().getIdSeccion()); s.setObject(3, inscripcion.getFechaInscripcion());
            if (inscripcion.getFechaRetiro() == null) s.setNull(4, Types.DATE); else s.setObject(4, inscripcion.getFechaRetiro());
            s.setBoolean(5, inscripcion.isActiva()); s.setInt(6, inscripcion.getIdInscripcion()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("actualizar la inscripción", e); }
    }
    public boolean retirar(int id, LocalDate fechaRetiro) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Inscripcion SET fecha_retiro = ?, activa = 0 WHERE id_inscripcion = ?")) { s.setObject(1, fechaRetiro); s.setInt(2, id); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("retirar la inscripción", e); }
    }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Inscripcion"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar inscripciones", e); } }

    private List<Inscripcion> consultar(String sql, Object... values) {
        List<Inscripcion> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) s.setInt(i + 1, (Integer) values[i]);
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); return resultado; }
        } catch (SQLException e) { throw JdbcSupport.error("consultar inscripciones", e); }
    }
    private Optional<Inscripcion> buscar(String sql, int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar la inscripción", e); }
    }
    private boolean existe(String sql, int idEstudiante, int idSeccion) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, idEstudiante); s.setInt(2, idSeccion); try (ResultSet rs = s.executeQuery()) { return rs.next(); } }
        catch (SQLException e) { throw JdbcSupport.error("verificar la inscripción", e); }
    }
    private Inscripcion mapear(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante(rs.getInt("id_estudiante"), rs.getString("carnet"), rs.getString("estudiante_nombres"), rs.getString("estudiante_apellidos"), rs.getString("estudiante_correo"), rs.getBoolean("estudiante_activo"));
        Curso curso = new Curso(rs.getInt("curso_id"), rs.getString("curso_codigo"), rs.getString("curso_nombre"), rs.getString("curso_descripcion"), rs.getInt("curso_creditos"), rs.getBoolean("curso_activo"));
        PeriodoAcademico periodo = new PeriodoAcademico(rs.getInt("periodo_id"), rs.getString("periodo_nombre"), JdbcSupport.localDate(rs, "periodo_inicio"), JdbcSupport.localDate(rs, "periodo_fin"), rs.getBoolean("periodo_activo"));
        Docente docente = new Docente(rs.getInt("docente_id"), rs.getString("docente_codigo"), rs.getString("docente_nombres"), rs.getString("docente_apellidos"), rs.getString("docente_correo"), rs.getBoolean("docente_activo"));
        Seccion seccion = new Seccion(rs.getInt("id_seccion"), rs.getString("seccion_codigo"), curso, periodo, docente, rs.getString("aula_asignada"), rs.getBoolean("seccion_activo"));
        return new Inscripcion(rs.getInt("id_inscripcion"), estudiante, seccion, JdbcSupport.localDate(rs, "fecha_inscripcion"), rs.getObject("fecha_retiro", LocalDate.class), rs.getBoolean("activa"));
    }
}
