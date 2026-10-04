package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HorarioSemanalRepository {
    private static final String SELECT = "SELECT h.id_horario, h.dia_semana, h.hora_inicio, h.hora_fin, " +
            "s.id_seccion, s.codigo, s.aula_asignada, s.activo, " +
            "c.id_curso AS curso_id, c.codigo AS curso_codigo, c.nombre AS curso_nombre, c.descripcion AS curso_descripcion, c.creditos AS curso_creditos, c.activo AS curso_activo, " +
            "p.id_periodo AS periodo_id, p.nombre AS periodo_nombre, p.fecha_inicio AS periodo_inicio, p.fecha_fin AS periodo_fin, p.activo AS periodo_activo, " +
            "d.id_docente AS docente_id, d.codigo_empleado AS docente_codigo, d.nombres AS docente_nombres, d.apellidos AS docente_apellidos, d.correo AS docente_correo, d.activo AS docente_activo " +
            "FROM HorarioSemanal h JOIN Seccion s ON s.id_seccion = h.id_seccion " +
            "JOIN Curso c ON c.id_curso = s.id_curso JOIN PeriodoAcademico p ON p.id_periodo = s.id_periodo " +
            "JOIN Docente d ON d.id_docente = s.id_docente ";

    public boolean guardar(HorarioSemanal horario) {
        String sql = "INSERT INTO HorarioSemanal (id_seccion, dia_semana, hora_inicio, hora_fin) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, horario.getSeccion().getIdSeccion()); s.setInt(2, horario.getDiaSemana().getValue()); s.setTime(3, Time.valueOf(horario.getHoraInicio())); s.setTime(4, Time.valueOf(horario.getHoraFin()));
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; horario.setIdHorario(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el horario semanal", e); }
    }
    public Optional<HorarioSemanal> buscarPorId(int id) { return buscar(SELECT + "WHERE h.id_horario = ?", id); }
    public List<HorarioSemanal> listar() { return consultar(SELECT + "ORDER BY h.id_seccion, h.dia_semana, h.hora_inicio"); }
    public List<HorarioSemanal> listarPorSeccion(int idSeccion) { return consultar(SELECT + "WHERE h.id_seccion = ?", idSeccion); }
    public boolean existe(int idSeccion, DayOfWeek dia, java.time.LocalTime inicio) {
        String sql = "SELECT 1 FROM HorarioSemanal WHERE id_seccion = ? AND dia_semana = ? AND hora_inicio = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, idSeccion); s.setInt(2, dia.getValue()); s.setTime(3, Time.valueOf(inicio)); try (ResultSet rs = s.executeQuery()) { return rs.next(); } }
        catch (SQLException e) { throw JdbcSupport.error("verificar el horario", e); }
    }
    public boolean actualizar(HorarioSemanal horario) {
        String sql = "UPDATE HorarioSemanal SET id_seccion = ?, dia_semana = ?, hora_inicio = ?, hora_fin = ? WHERE id_horario = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, horario.getSeccion().getIdSeccion()); s.setInt(2, horario.getDiaSemana().getValue()); s.setTime(3, Time.valueOf(horario.getHoraInicio())); s.setTime(4, Time.valueOf(horario.getHoraFin())); s.setInt(5, horario.getIdHorario()); return s.executeUpdate() > 0; }
        catch (SQLException e) { throw JdbcSupport.error("actualizar el horario", e); }
    }

    private List<HorarioSemanal> consultar(String sql, Object... values) {
        List<HorarioSemanal> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) s.setInt(i + 1, (Integer) values[i]);
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); return resultado; }
        } catch (SQLException e) { throw JdbcSupport.error("consultar horarios", e); }
    }
    private Optional<HorarioSemanal> buscar(String sql, int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar el horario", e); }
    }
    private HorarioSemanal mapear(ResultSet rs) throws SQLException {
        return new HorarioSemanal(rs.getInt("id_horario"), SeccionRepository.mapear(rs), DayOfWeek.of(rs.getInt("dia_semana")), JdbcSupport.localTime(rs, "hora_inicio"), JdbcSupport.localTime(rs, "hora_fin"));
    }
}
