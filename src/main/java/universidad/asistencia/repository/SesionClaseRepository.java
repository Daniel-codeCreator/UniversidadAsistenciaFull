package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.model.*;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SesionClaseRepository {
    private static final String SELECT = "SELECT x.id_sesion, x.fecha, x.hora_inicio_programada, x.hora_fin_programada, x.aula, x.estado, " +
            "s.id_seccion, s.codigo AS seccion_codigo, s.aula_asignada, s.activo AS seccion_activo, " +
            "c.id_curso AS curso_id, c.codigo AS curso_codigo, c.nombre AS curso_nombre, c.descripcion AS curso_descripcion, c.creditos AS curso_creditos, c.activo AS curso_activo, " +
            "p.id_periodo AS periodo_id, p.nombre AS periodo_nombre, p.fecha_inicio AS periodo_inicio, p.fecha_fin AS periodo_fin, p.activo AS periodo_activo, " +
            "d.id_docente AS docente_id, d.codigo_empleado AS docente_codigo, d.nombres AS docente_nombres, d.apellidos AS docente_apellidos, d.correo AS docente_correo, d.activo AS docente_activo " +
            "FROM SesionClase x JOIN Seccion s ON s.id_seccion = x.id_seccion JOIN Curso c ON c.id_curso = s.id_curso " +
            "JOIN PeriodoAcademico p ON p.id_periodo = s.id_periodo JOIN Docente d ON d.id_docente = s.id_docente ";

    public boolean guardar(SesionClase sesion) {
        String sql = "INSERT INTO SesionClase (id_seccion, fecha, hora_inicio_programada, hora_fin_programada, aula, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, sesion.getSeccion().getIdSeccion()); s.setObject(2, sesion.getFecha()); s.setTime(3, Time.valueOf(sesion.getHoraInicioProgramada())); s.setTime(4, Time.valueOf(sesion.getHoraFinProgramada())); s.setString(5, sesion.getAula()); s.setString(6, sesion.getEstado().name());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; sesion.setIdSesion(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar la sesión de clase", e); }
    }
    public Optional<SesionClase> buscarPorId(int id) { return buscar(SELECT + "WHERE x.id_sesion = ?", id); }
    public List<SesionClase> listar() { return consultar(SELECT + "ORDER BY x.fecha, x.hora_inicio_programada"); }
    public List<SesionClase> listarPorSeccion(int idSeccion) { return consultar(SELECT + "WHERE x.id_seccion = ? ORDER BY x.fecha, x.hora_inicio_programada", idSeccion); }
    public Optional<SesionClase> buscarPorSeccionFechaHora(int idSeccion, LocalDate fecha, LocalTime horaInicio) {
        String sql = SELECT + "WHERE x.id_seccion = ? AND x.fecha = ? AND x.hora_inicio_programada = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idSeccion); s.setObject(2, fecha); s.setTime(3, Time.valueOf(horaInicio));
            try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar la sesión de clase", e); }
    }
    public boolean actualizar(SesionClase sesion) {
        String sql = "UPDATE SesionClase SET id_seccion = ?, fecha = ?, hora_inicio_programada = ?, hora_fin_programada = ?, aula = ?, estado = ? WHERE id_sesion = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, sesion.getSeccion().getIdSeccion()); s.setObject(2, sesion.getFecha()); s.setTime(3, Time.valueOf(sesion.getHoraInicioProgramada())); s.setTime(4, Time.valueOf(sesion.getHoraFinProgramada())); s.setString(5, sesion.getAula()); s.setString(6, sesion.getEstado().name()); s.setInt(7, sesion.getIdSesion()); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("actualizar la sesión de clase", e); }
    }
    public boolean actualizarEstado(int idSesion, EstadoSesion estado) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE SesionClase SET estado = ? WHERE id_sesion = ?")) { s.setString(1, estado.name()); s.setInt(2, idSesion); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("actualizar el estado de la sesión", e); }
    }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM SesionClase"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar sesiones", e); } }

    private List<SesionClase> consultar(String sql, Object... values) {
        List<SesionClase> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) s.setInt(i + 1, (Integer) values[i]);
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); return resultado; }
        } catch (SQLException e) { throw JdbcSupport.error("consultar sesiones", e); }
    }
    private Optional<SesionClase> buscar(String sql, int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar la sesión de clase", e); }
    }
    private SesionClase mapear(ResultSet rs) throws SQLException {
        Curso curso = new Curso(rs.getInt("curso_id"), rs.getString("curso_codigo"), rs.getString("curso_nombre"), rs.getString("curso_descripcion"), rs.getInt("curso_creditos"), rs.getBoolean("curso_activo"));
        PeriodoAcademico periodo = new PeriodoAcademico(rs.getInt("periodo_id"), rs.getString("periodo_nombre"), JdbcSupport.localDate(rs, "periodo_inicio"), JdbcSupport.localDate(rs, "periodo_fin"), rs.getBoolean("periodo_activo"));
        Docente docente = new Docente(rs.getInt("docente_id"), rs.getString("docente_codigo"), rs.getString("docente_nombres"), rs.getString("docente_apellidos"), rs.getString("docente_correo"), rs.getBoolean("docente_activo"));
        Seccion seccion = new Seccion(rs.getInt("id_seccion"), rs.getString("seccion_codigo"), curso, periodo, docente, rs.getString("aula_asignada"), rs.getBoolean("seccion_activo"));
        return new SesionClase(rs.getInt("id_sesion"), seccion, JdbcSupport.localDate(rs, "fecha"), JdbcSupport.localTime(rs, "hora_inicio_programada"), JdbcSupport.localTime(rs, "hora_fin_programada"), rs.getString("aula"), EstadoSesion.valueOf(rs.getString("estado")));
    }
}
