package universidad.asistencia.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** Conversiones entre los tipos de fecha de Swing/JCalendar y el modelo. */
public final class DateUtils {

    private DateUtils() {
    }

    public static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date toDate(LocalDate date) {
        return date == null ? null : java.sql.Date.valueOf(date);
    }
}
