package universidad.asistencia.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Utilidades pequeñas para que los repositorios mantengan el mismo manejo de errores. */
public final class JdbcSupport {
    private JdbcSupport() { }

    public static DataAccessException error(String operation, SQLException cause) {
        return new DataAccessException("No se pudo " + operation + ".", cause);
    }

    public static LocalDate localDate(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getObject(column, LocalDate.class);
    }

    public static LocalTime localTime(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getObject(column, LocalTime.class);
    }

    public static LocalDateTime localDateTime(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getObject(column, LocalDateTime.class);
    }
}
