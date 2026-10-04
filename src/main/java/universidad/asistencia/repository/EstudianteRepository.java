package universidad.asistencia.repository;

import universidad.asistencia.config.DatabaseConnection;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.util.JdbcSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstudianteRepository {

    public boolean guardar(Estudiante estudiante) {

        String sql = """
                INSERT INTO Estudiante
                (
                    carnet,
                    nombres,
                    apellidos,
                    correo,
                    activo
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, estudiante.getCarnet());
            statement.setString(2, estudiante.getNombres());
            statement.setString(3, estudiante.getApellidos());
            statement.setString(4, estudiante.getCorreo());
            statement.setBoolean(5, estudiante.isActivo());
            if (statement.executeUpdate() == 0) return false;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) return false;
                estudiante.setIdEstudiante(generatedKeys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            throw JdbcSupport.error("guardar el estudiante", e);
        }
    }

    public List<Estudiante> listar() {
        return consultar("SELECT id_estudiante, carnet, nombres, apellidos, correo, activo " +
                "FROM Estudiante ORDER BY carnet ASC");
    }

    public List<Estudiante> listarActivos() {
        return consultar("SELECT id_estudiante, carnet, nombres, apellidos, correo, activo " +
                "FROM Estudiante WHERE activo = 1 ORDER BY carnet ASC");
    }

    public Optional<Estudiante> buscarPorId(int id) {
        return buscar("SELECT id_estudiante, carnet, nombres, apellidos, correo, activo " +
                "FROM Estudiante WHERE id_estudiante = ?", statement -> statement.setInt(1, id));
    }

    public Optional<Estudiante> buscarPorCarnet(String carnet) {
        return buscar("SELECT id_estudiante, carnet, nombres, apellidos, correo, activo " +
                "FROM Estudiante WHERE carnet = ?", statement -> statement.setString(1, carnet));
    }

    public boolean actualizar(Estudiante estudiante) {
        String sql = "UPDATE Estudiante SET carnet = ?, nombres = ?, apellidos = ?, correo = ?, activo = ? " +
                "WHERE id_estudiante = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estudiante.getCarnet());
            statement.setString(2, estudiante.getNombres());
            statement.setString(3, estudiante.getApellidos());
            statement.setString(4, estudiante.getCorreo());
            statement.setBoolean(5, estudiante.isActivo());
            statement.setInt(6, estudiante.getIdEstudiante());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcSupport.error("actualizar el estudiante", e);
        }
    }

    public boolean desactivar(int id) {
        return cambiarActivo(id, false);
    }

    public boolean existeCarnet(String carnet) {
        return existe("SELECT 1 FROM Estudiante WHERE carnet = ?", carnet);
    }

    public boolean existeCarnet(String carnet, int idExcluido) {
        return existe("SELECT 1 FROM Estudiante WHERE carnet = ? AND id_estudiante <> ?", carnet, idExcluido);
    }

    public boolean existeCorreo(String correo) {
        return existe("SELECT 1 FROM Estudiante WHERE correo = ?", correo);
    }

    public boolean existeCorreo(String correo, int idExcluido) {
        return existe("SELECT 1 FROM Estudiante WHERE correo = ? AND id_estudiante <> ?", correo, idExcluido);
    }

    public int contar() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM Estudiante");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw JdbcSupport.error("contar estudiantes", e);
        }
    }

    private boolean cambiarActivo(int id, boolean activo) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE Estudiante SET activo = ? WHERE id_estudiante = ?")) {
            statement.setBoolean(1, activo);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcSupport.error("cambiar el estado del estudiante", e);
        }
    }

    private List<Estudiante> consultar(String sql) {
        List<Estudiante> estudiantes = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) estudiantes.add(mapearEstudiante(resultSet));
            return estudiantes;
        } catch (SQLException e) {
            throw JdbcSupport.error("consultar estudiantes", e);
        }
    }

    private Optional<Estudiante> buscar(String sql, SqlParameter parameter) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            parameter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapearEstudiante(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.error("buscar el estudiante", e);
        }
    }

    private boolean existe(String sql, Object... values) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer integer) statement.setInt(i + 1, integer);
                else statement.setString(i + 1, (String) values[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next(); }
        } catch (SQLException e) {
            throw JdbcSupport.error("verificar el estudiante", e);
        }
    }

    private Estudiante mapearEstudiante(ResultSet resultSet) throws SQLException {
        return new Estudiante(resultSet.getInt("id_estudiante"), resultSet.getString("carnet"),
                resultSet.getString("nombres"), resultSet.getString("apellidos"),
                resultSet.getString("correo"), resultSet.getBoolean("activo"));
    }

    @FunctionalInterface
    private interface SqlParameter { void set(PreparedStatement statement) throws SQLException; }
}
