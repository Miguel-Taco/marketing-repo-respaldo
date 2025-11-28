package pe.unmsm.crm.marketing.shared.utils;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtils {

    public Instant nowUtc() {
        return Instant.now();
    }

    public LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    // Formato estándar para fechas en respuestas API (ISO-8601)
    public String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
