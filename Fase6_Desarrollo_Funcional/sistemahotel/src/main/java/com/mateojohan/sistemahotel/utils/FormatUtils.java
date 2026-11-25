package com.mateojohan.sistemahotel.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos para los mensajes al usuario final: fechas dd/mm/aaaa y moneda COP.
 * La API (JSON) usa ISO 8601; estos formatos solo se usan dentro de los textos de error.
 */
public final class FormatUtils {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale COLOMBIA = Locale.of("es", "CO");

    private FormatUtils() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }

    public static String formatCop(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(COLOMBIA);
        format.setMaximumFractionDigits(0);
        return "$ " + format.format(amount == null ? BigDecimal.ZERO : amount) + " COP";
    }
}
