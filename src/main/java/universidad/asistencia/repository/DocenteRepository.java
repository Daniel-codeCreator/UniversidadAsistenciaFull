package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.Docente;
import universidad.asistencia.util.JdbcSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocenteRepository {
    private static final String COLUMNAS = "id_docente, codigo_empleado, nombres, apellidos, correo, activo";

    public boolean guardar(Docente docente) {
        String sql = "INSERT INTO Docente (codigo_empleado, nombres, apellidos, correo, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, docente.getCodigoEmpleado());
            statement.setString(2, docente.getNombres());
            statement.setString(3, docente.getApellidos());
            statement.setString(4, docente.getCorreo());
            statement.setBoolean(5, docente.isActivo());
            if (statement.executeUpdate() == 0) return false;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) return false;
                docente.setIdDocente(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { throw JdbcSupport.error("guardar el docente", e); }
    }

    public List<Docente> listar() { return consultar("SELECT " + COLUMNAS + " FROM Docente ORDER BY apellidos, nombres"); }
    public List<Docente> listarActivos() { return consultar("SELECT " + COLUMNAS + " FROM Docente WHERE activo = 1 ORDER BY apellidos, nombres"); }

    public Optional<Docente> buscarPorId(int id) {
        return buscar("SELECT " + COLUMNAS + " FROM Docente WHERE id_docente = ?", id);
    }

    public Optional<Docente> buscarPorCodigo(String codigo) {
        return buscar("SELECT " + COLUMNAS + " FROM Docente WHERE codigo_empleado = ?", codigo);
    }

    public boolean actualizar(Docente docente) {
        String sql = "UPDATE Docente SET codigo_empleado = ?, nombres = ?, apellidos = ?, correo = ?, activo = ? WHERE id_docente = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, docente.getCodigoEmpleado());
            statement.setString(2, docente.getNombres());
            statement.setString(3, docente.getApellidos());
            statement.setString(4, docente.getCorreo());
            statement.setBoolean(5, docente.isActivo());
            statement.setInt(6, docente.getIdDocente());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("actualizar el docente", e); }
    }

    public boolean desactivar(int id) { return cambiarActivo(id, false); }
    public boolean existeCodigo(String codigo) { return existe("SELECT 1 FROM Docente WHERE codigo_empleado = ?", codigo); }
    public boolean existeCodigo(String codigo, int idExcluido) { return existe("SELECT 1 FROM Docente WHERE codigo_empleado = ? AND id_docente <> ?", codigo, idExcluido); }
    public boolean existeCorreo(String correo) { return existe("SELECT 1 FROM Docente WHERE correo = ?", correo); }
    public boolean existeCorreo(String correo, int idExcluido) { return existe("SELECT 1 FROM Docente WHERE correo = ? AND id_docente <> ?", correo, idExcluido); }
    public int contar() { return contar("SELECT COUNT(*) FROM Docente"); }

    private List<Docente> consultar(String sql) {
        List<Docente> resultado = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) resultado.add(mapear(rs));
            return resultado;
        } catch (SQLException e) { throw JdbcSupport.error("consultar docentes", e); }
    }

    private Optional<Docente> buscar(String sql, Object valor) {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (valor instanceof Integer id) statement.setInt(1, id); else statement.setString(1, (String) valor);
            try (ResultSet rs = statement.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcSupport.error("buscar el docente", e); }
    }

    private boolean existe(String sql, Object... valores) {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                if (valores[i] instanceof Integer id) statement.setInt(i + 1, id); else statement.setString(i + 1, (String) valores[i]);
            }
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw JdbcSupport.error("verificar el docente", e); }
    }

    private boolean cambiarActivo(int id, boolean activo) {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE Docente SET activo = ? WHERE id_docente = ?")) {
            statement.setBoolean(1, activo); statement.setInt(2, id); return statement.executeUpdate() > 0;
        } catch (SQLException e) { throw JdbcSupport.error("desactivar el docente", e); }
    }

    private int contar(String sql) {
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getInt(1);
        } catch (SQLException e) { throw JdbcSupport.error("contar docentes", e); }
    }

    private Docente mapear(ResultSet rs) throws SQLException {
        return new Docente(rs.getInt("id_docente"), rs.getString("codigo_empleado"), rs.getString("nombres"),
                rs.getString("apellidos"), rs.getString("correo"), rs.getBoolean("activo"));
    }
}
