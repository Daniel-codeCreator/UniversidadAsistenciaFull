package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.enums.EstadoJustificacion;
import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.model.*;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JustificacionRepository {
    private static final String SELECT = "SELECT j.id_justificacion, j.motivo, j.fecha, j.observacion, j.evidencia_nombre, j.evidencia_tipo, j.evidencia, j.estado, j.fecha_resolucion, " +
            "e.id_estudiante, e.carnet, e.nombres AS estudiante_nombres, e.apellidos AS estudiante_apellidos, e.correo AS estudiante_correo, e.activo AS estudiante_activo, " +
            "x.id_sesion, x.fecha AS sesion_fecha, x.hora_inicio_programada, x.hora_fin_programada, x.aula AS sesion_aula, x.estado AS sesion_estado, s.id_seccion, s.codigo AS seccion_codigo, s.aula_asignada, s.activo AS seccion_activo, " +
            "c.id_curso AS curso_id, c.codigo AS curso_codigo, c.nombre AS curso_nombre, c.descripcion AS curso_descripcion, c.creditos AS curso_creditos, c.activo AS curso_activo, " +
            "p.id_periodo AS periodo_id, p.nombre AS periodo_nombre, p.fecha_inicio AS periodo_inicio, p.fecha_fin AS periodo_fin, p.activo AS periodo_activo, " +
            "sd.id_docente AS seccion_docente_id, sd.codigo_empleado AS seccion_docente_codigo, sd.nombres AS seccion_docente_nombres, sd.apellidos AS seccion_docente_apellidos, sd.correo AS seccion_docente_correo, sd.activo AS seccion_docente_activo, " +
            "dr.id_docente AS registra_id, dr.codigo_empleado AS registra_codigo, dr.nombres AS registra_nombres, dr.apellidos AS registra_apellidos, dr.correo AS registra_correo, dr.activo AS registra_activo, " +
            "dv.id_docente AS resuelve_id, dv.codigo_empleado AS resuelve_codigo, dv.nombres AS resuelve_nombres, dv.apellidos AS resuelve_apellidos, dv.correo AS resuelve_correo, dv.activo AS resuelve_activo " +
            "FROM Justificacion j JOIN Estudiante e ON e.id_estudiante = j.id_estudiante JOIN SesionClase x ON x.id_sesion = j.id_sesion JOIN Seccion s ON s.id_seccion = x.id_seccion " +
            "JOIN Curso c ON c.id_curso = s.id_curso JOIN PeriodoAcademico p ON p.id_periodo = s.id_periodo JOIN Docente sd ON sd.id_docente = s.id_docente " +
            "JOIN Docente dr ON dr.id_docente = j.id_docente_registra LEFT JOIN Docente dv ON dv.id_docente = j.id_docente_resuelve ";

    public boolean guardar(Justificacion j) {
        String sql = "INSERT INTO Justificacion (id_estudiante, id_sesion, id_docente_registra, motivo, observacion, evidencia_nombre, evidencia_tipo, evidencia, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, j.getEstudiante().getIdEstudiante()); s.setInt(2, j.getSesionClase().getIdSesion()); s.setInt(3, j.getDocenteRegistra().getIdDocente()); s.setString(4, j.getMotivo()); s.setString(5, j.getObservacion()); s.setString(6, j.getEvidenciaNombre()); s.setString(7, j.getEvidenciaTipo());
            if (j.getEvidencia() == null) s.setNull(8, Types.VARBINARY); else s.setBytes(8, j.getEvidencia());
            s.setString(9, j.getEstado().name());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; j.setIdJustificacion(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar la justificación", e); }
    }
    public Optional<Justificacion> buscarPorId(int id) { return buscar(SELECT + "WHERE j.id_justificacion = ?", id); }
    public Optional<Justificacion> buscarPorEstudianteYSesion(int idEstudiante, int idSesion) {
        String sql = SELECT + "WHERE j.id_estudiante = ? AND j.id_sesion = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, idEstudiante); s.setInt(2, idSesion); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar la justificación", e); }
    }
    public List<Justificacion> listar() { return consultar(SELECT + "ORDER BY j.fecha DESC"); }
    public List<Justificacion> listarPorEstado(EstadoJustificacion estado) {
        String sql = SELECT + "WHERE j.estado = ? ORDER BY j.fecha DESC";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setString(1, estado.name()); try (ResultSet rs = s.executeQuery()) { List<Justificacion> r = new ArrayList<>(); while (rs.next()) r.add(mapear(rs)); return r; } }
        catch (SQLException e) { throw JdbcSupport.error("consultar justificaciones", e); }
    }
    public boolean existe(int idEstudiante, int idSesion) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT 1 FROM Justificacion WHERE id_estudiante = ? AND id_sesion = ?")) { s.setInt(1, idEstudiante); s.setInt(2, idSesion); try (ResultSet rs = s.executeQuery()) { return rs.next(); } }
        catch (SQLException e) { throw JdbcSupport.error("verificar la justificación", e); }
    }
    public boolean resolver(int idJustificacion, EstadoJustificacion estado, int idDocenteResuelve) {
        String sql = "UPDATE Justificacion SET estado = ?, id_docente_resuelve = ?, fecha_resolucion = SYSDATETIME() WHERE id_justificacion = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setString(1, estado.name()); s.setInt(2, idDocenteResuelve); s.setInt(3, idJustificacion); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("resolver la justificación", e); }
    }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Justificacion"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar justificaciones", e); } }

    private List<Justificacion> consultar(String sql) {
        List<Justificacion> r = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet rs = s.executeQuery()) { while (rs.next()) r.add(mapear(rs)); return r; }
        catch (SQLException e) { throw JdbcSupport.error("consultar justificaciones", e); }
    }
    private Optional<Justificacion> buscar(String sql, int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar la justificación", e); }
    }
    private Justificacion mapear(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante(rs.getInt("id_estudiante"), rs.getString("carnet"), rs.getString("estudiante_nombres"), rs.getString("estudiante_apellidos"), rs.getString("estudiante_correo"), rs.getBoolean("estudiante_activo"));
        Curso curso = new Curso(rs.getInt("curso_id"), rs.getString("curso_codigo"), rs.getString("curso_nombre"), rs.getString("curso_descripcion"), rs.getInt("curso_creditos"), rs.getBoolean("curso_activo"));
        PeriodoAcademico periodo = new PeriodoAcademico(rs.getInt("periodo_id"), rs.getString("periodo_nombre"), JdbcSupport.localDate(rs, "periodo_inicio"), JdbcSupport.localDate(rs, "periodo_fin"), rs.getBoolean("periodo_activo"));
        Docente docenteSeccion = new Docente(rs.getInt("seccion_docente_id"), rs.getString("seccion_docente_codigo"), rs.getString("seccion_docente_nombres"), rs.getString("seccion_docente_apellidos"), rs.getString("seccion_docente_correo"), rs.getBoolean("seccion_docente_activo"));
        Seccion seccion = new Seccion(rs.getInt("id_seccion"), rs.getString("seccion_codigo"), curso, periodo, docenteSeccion, rs.getString("aula_asignada"), rs.getBoolean("seccion_activo"));
        SesionClase sesion = new SesionClase(rs.getInt("id_sesion"), seccion, JdbcSupport.localDate(rs, "sesion_fecha"), JdbcSupport.localTime(rs, "hora_inicio_programada"), JdbcSupport.localTime(rs, "hora_fin_programada"), rs.getString("sesion_aula"), EstadoSesion.valueOf(rs.getString("sesion_estado")));
        Docente registra = new Docente(rs.getInt("registra_id"), rs.getString("registra_codigo"), rs.getString("registra_nombres"), rs.getString("registra_apellidos"), rs.getString("registra_correo"), rs.getBoolean("registra_activo"));
        Docente resuelve = null;
        if (rs.getObject("resuelve_id") != null) resuelve = new Docente(rs.getInt("resuelve_id"), rs.getString("resuelve_codigo"), rs.getString("resuelve_nombres"), rs.getString("resuelve_apellidos"), rs.getString("resuelve_correo"), rs.getBoolean("resuelve_activo"));
        return new Justificacion(rs.getInt("id_justificacion"), estudiante, sesion, registra, rs.getString("motivo"), rs.getObject("fecha", LocalDateTime.class), rs.getString("observacion"), rs.getString("evidencia_nombre"), rs.getString("evidencia_tipo"), rs.getBytes("evidencia"), EstadoJustificacion.valueOf(rs.getString("estado")), resuelve, rs.getObject("fecha_resolucion", LocalDateTime.class));
    }
}
