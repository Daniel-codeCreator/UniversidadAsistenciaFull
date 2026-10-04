package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.enums.TipoMarcaje;
import universidad.asistencia.model.*;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarcajeRepository {
    private static final String SELECT = "SELECT m.id_marcaje, m.fecha_hora, m.tipo, m.medio, " +
            "e.id_estudiante, e.carnet, e.nombres AS estudiante_nombres, e.apellidos AS estudiante_apellidos, e.correo AS estudiante_correo, e.activo AS estudiante_activo, " +
            "x.id_sesion, x.fecha AS sesion_fecha, x.hora_inicio_programada, x.hora_fin_programada, x.aula AS sesion_aula, x.estado AS sesion_estado, " +
            "s.id_seccion, s.codigo AS seccion_codigo, s.aula_asignada, s.activo AS seccion_activo, " +
            "c.id_curso AS curso_id, c.codigo AS curso_codigo, c.nombre AS curso_nombre, c.descripcion AS curso_descripcion, c.creditos AS curso_creditos, c.activo AS curso_activo, " +
            "p.id_periodo AS periodo_id, p.nombre AS periodo_nombre, p.fecha_inicio AS periodo_inicio, p.fecha_fin AS periodo_fin, p.activo AS periodo_activo, " +
            "sd.id_docente AS docente_id, sd.codigo_empleado AS docente_codigo, sd.nombres AS docente_nombres, sd.apellidos AS docente_apellidos, sd.correo AS docente_correo, sd.activo AS docente_activo, " +
            "d.id_dispositivo, d.codigo AS dispositivo_codigo, d.nombre AS dispositivo_nombre, d.tipo AS dispositivo_tipo, d.ubicacion AS dispositivo_ubicacion, d.activo AS dispositivo_activo " +
            "FROM Marcaje m JOIN Estudiante e ON e.id_estudiante = m.id_estudiante " +
            "JOIN SesionClase x ON x.id_sesion = m.id_sesion JOIN Seccion s ON s.id_seccion = x.id_seccion " +
            "JOIN Curso c ON c.id_curso = s.id_curso JOIN PeriodoAcademico p ON p.id_periodo = s.id_periodo " +
            "JOIN Docente sd ON sd.id_docente = s.id_docente JOIN Dispositivo d ON d.id_dispositivo = m.id_dispositivo ";

    public boolean guardar(Marcaje marcaje) {
        String sql = "INSERT INTO Marcaje (id_estudiante, id_sesion, id_dispositivo, tipo, medio) " +
                "OUTPUT INSERTED.id_marcaje, INSERTED.fecha_hora VALUES (?, ?, ?, ?, ?)";
        return guardarConFecha(marcaje, sql, false);
    }

    /** Se usa en datos históricos de prueba; la operación normal deja que SQL Server genere la hora. */
    public boolean guardarConFecha(Marcaje marcaje) {
        String sql = "INSERT INTO Marcaje (id_estudiante, id_sesion, id_dispositivo, fecha_hora, tipo, medio) " +
                "OUTPUT INSERTED.id_marcaje, INSERTED.fecha_hora VALUES (?, ?, ?, ?, ?, ?)";
        return guardarConFecha(marcaje, sql, true);
    }

    private boolean guardarConFecha(Marcaje m, String sql, boolean incluyeFecha) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, m.getEstudiante().getIdEstudiante()); s.setInt(2, m.getSesionClase().getIdSesion()); s.setInt(3, m.getDispositivo().getIdDispositivo());
            int offset = 4;
            if (incluyeFecha) { s.setObject(offset++, m.getFechaHora()); }
            s.setString(offset++, m.getTipo().name()); s.setString(offset, m.getMedio().name());
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return false;
                m.setIdMarcaje(rs.getLong("id_marcaje")); m.setFechaHora(rs.getObject("fecha_hora", LocalDateTime.class)); return true;
            }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el marcaje", e); }
    }

    public Optional<Marcaje> buscarPorId(long id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(SELECT + "WHERE m.id_marcaje = ?")) { s.setLong(1, id); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar el marcaje", e); }
    }
    public List<Marcaje> listar() { return consultar(SELECT + "ORDER BY m.fecha_hora"); }
    public List<Marcaje> listarPorEstudianteYSesion(int idEstudiante, int idSesion) { return consultar(SELECT + "WHERE m.id_estudiante = ? AND m.id_sesion = ? ORDER BY m.fecha_hora", idEstudiante, idSesion); }
    public List<Marcaje> listarHistorial(int idEstudiante) { return consultar(SELECT + "WHERE m.id_estudiante = ? ORDER BY m.fecha_hora DESC", idEstudiante); }
    public Optional<Marcaje> obtenerUltimo(int idEstudiante, int idSesion) {
        List<Marcaje> marcajes = listarPorEstudianteYSesion(idEstudiante, idSesion);
        return marcajes.isEmpty() ? Optional.empty() : Optional.of(marcajes.get(marcajes.size() - 1));
    }
    public Optional<Marcaje> obtenerPrimeraEntrada(int idEstudiante, int idSesion) {
        String sql = SELECT + "WHERE m.id_estudiante = ? AND m.id_sesion = ? AND m.tipo = 'ENTRADA' ORDER BY m.fecha_hora";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, idEstudiante); s.setInt(2, idSesion); try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); } }
        catch (SQLException e) { throw JdbcSupport.error("buscar la primera entrada", e); }
    }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Marcaje"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar marcajes", e); } }

    private List<Marcaje> consultar(String sql, Object... values) {
        List<Marcaje> r = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) s.setInt(i + 1, (Integer) values[i]);
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) r.add(mapear(rs)); return r; }
        } catch (SQLException e) { throw JdbcSupport.error("consultar marcajes", e); }
    }
    private Marcaje mapear(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante(rs.getInt("id_estudiante"), rs.getString("carnet"), rs.getString("estudiante_nombres"), rs.getString("estudiante_apellidos"), rs.getString("estudiante_correo"), rs.getBoolean("estudiante_activo"));
        Curso curso = new Curso(rs.getInt("curso_id"), rs.getString("curso_codigo"), rs.getString("curso_nombre"), rs.getString("curso_descripcion"), rs.getInt("curso_creditos"), rs.getBoolean("curso_activo"));
        PeriodoAcademico periodo = new PeriodoAcademico(rs.getInt("periodo_id"), rs.getString("periodo_nombre"), JdbcSupport.localDate(rs, "periodo_inicio"), JdbcSupport.localDate(rs, "periodo_fin"), rs.getBoolean("periodo_activo"));
        Docente docente = new Docente(rs.getInt("docente_id"), rs.getString("docente_codigo"), rs.getString("docente_nombres"), rs.getString("docente_apellidos"), rs.getString("docente_correo"), rs.getBoolean("docente_activo"));
        Seccion seccion = new Seccion(rs.getInt("id_seccion"), rs.getString("seccion_codigo"), curso, periodo, docente, rs.getString("aula_asignada"), rs.getBoolean("seccion_activo"));
        SesionClase sesion = new SesionClase(rs.getInt("id_sesion"), seccion, JdbcSupport.localDate(rs, "sesion_fecha"), JdbcSupport.localTime(rs, "hora_inicio_programada"), JdbcSupport.localTime(rs, "hora_fin_programada"), rs.getString("sesion_aula"), universidad.asistencia.enums.EstadoSesion.valueOf(rs.getString("sesion_estado")));
        Dispositivo dispositivo = new Dispositivo(rs.getInt("id_dispositivo"), rs.getString("dispositivo_codigo"), rs.getString("dispositivo_nombre"), rs.getString("dispositivo_tipo"), rs.getString("dispositivo_ubicacion"), rs.getBoolean("dispositivo_activo"));
        return new Marcaje(rs.getLong("id_marcaje"), estudiante, sesion, dispositivo, JdbcSupport.localDateTime(rs, "fecha_hora"), TipoMarcaje.valueOf(rs.getString("tipo")), MedioMarcaje.valueOf(rs.getString("medio")));
    }
}
