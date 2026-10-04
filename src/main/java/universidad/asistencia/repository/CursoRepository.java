package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.Curso;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CursoRepository {
    private static final String COLUMNAS = "id_curso, codigo, nombre, descripcion, creditos, activo";

    public boolean guardar(Curso curso) {
        String sql = "INSERT INTO Curso (codigo, nombre, descripcion, creditos, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, curso.getCodigo()); s.setString(2, curso.getNombre()); s.setString(3, curso.getDescripcion());
            s.setInt(4, curso.getCreditos()); s.setBoolean(5, curso.isActivo());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (!keys.next()) return false; curso.setIdCurso(keys.getInt(1)); return true; }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el curso", e); }
    }

    public List<Curso> listar() { return consultar("SELECT " + COLUMNAS + " FROM Curso ORDER BY codigo"); }
    public List<Curso> listarActivos() { return consultar("SELECT " + COLUMNAS + " FROM Curso WHERE activo = 1 ORDER BY codigo"); }
    public Optional<Curso> buscarPorId(int id) { return buscar("SELECT " + COLUMNAS + " FROM Curso WHERE id_curso = ?", id); }
    public Optional<Curso> buscarPorCodigo(String codigo) { return buscar("SELECT " + COLUMNAS + " FROM Curso WHERE codigo = ?", codigo); }

    public boolean actualizar(Curso curso) {
        String sql = "UPDATE Curso SET codigo = ?, nombre = ?, descripcion = ?, creditos = ?, activo = ? WHERE id_curso = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, curso.getCodigo()); s.setString(2, curso.getNombre()); s.setString(3, curso.getDescripcion());
            s.setInt(4, curso.getCreditos()); s.setBoolean(5, curso.isActivo()); s.setInt(6, curso.getIdCurso()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("actualizar el curso", e); }
    }

    public boolean desactivar(int id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Curso SET activo = 0 WHERE id_curso = ?")) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("desactivar el curso", e); }
    }
    public boolean existeCodigo(String codigo) { return existe("SELECT 1 FROM Curso WHERE codigo = ?", codigo); }
    public boolean existeCodigo(String codigo, int idExcluido) { return existe("SELECT 1 FROM Curso WHERE codigo = ? AND id_curso <> ?", codigo, idExcluido); }
    public int contar() { try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM Curso"); ResultSet rs = s.executeQuery()) { rs.next(); return rs.getInt(1); } catch (SQLException e) { throw JdbcSupport.error("contar cursos", e); } }

    private List<Curso> consultar(String sql) {
        List<Curso> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet rs = s.executeQuery()) {
            while (rs.next()) resultado.add(mapear(rs)); return resultado;
        } catch (SQLException e) { throw JdbcSupport.error("consultar cursos", e); }
    }
    private Optional<Curso> buscar(String sql, Object valor) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            if (valor instanceof Integer id) s.setInt(1, id); else s.setString(1, (String) valor);
            try (ResultSet rs = s.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar el curso", e); }
    }
    private boolean existe(String sql, Object... values) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) { if (values[i] instanceof Integer id) s.setInt(i + 1, id); else s.setString(i + 1, (String) values[i]); }
            try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw JdbcSupport.error("verificar el curso", e); }
    }
    private Curso mapear(ResultSet rs) throws SQLException {
        return new Curso(rs.getInt("id_curso"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getInt("creditos"), rs.getBoolean("activo"));
    }
}
