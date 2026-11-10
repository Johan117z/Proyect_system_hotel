package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Scanner;

/**
 * Lectura validada desde consola y formato de salida (fechas dd/mm/aaaa, moneda COP).
 */
public final class ConsoleUtils {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter UI_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Locale CO = Locale.forLanguageTag("es-CO");

    private ConsoleUtils() {
    }

    public static String readText(String message) {
        while (true) {
            System.out.print(message);
            String input = SCANNER.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  ! El campo no puede estar vacío.");
        }
    }

    public static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ! Ingrese un número entero válido.");
            }
        }
    }

    /** Lee un valor en pesos. Acepta "120000", "120.000" o "120000,50". */
    public static BigDecimal readMoney(String message, boolean allowZero) {
        while (true) {
            System.out.print(message);
            String input = SCANNER.nextLine().trim().replace("$", "").replace(" ", "");
            String normalized = input.contains(",")
                    ? input.replace(".", "").replace(",", ".")
                    : input.replace(".", "");
            try {
                BigDecimal value = new BigDecimal(normalized);
                if (value.signum() > 0 || (allowZero && value.signum() == 0)) {
                    return value;
                }
                System.out.println(allowZero ? "  ! El valor no puede ser negativo." : "  ! El valor debe ser mayor que cero.");
            } catch (NumberFormatException e) {
                System.out.println("  ! Ingrese un valor numérico válido.");
            }
        }
    }

    public static LocalDate readDate(String message) {
        while (true) {
            System.out.print(message + " (dd/mm/aaaa): ");
            try {
                return LocalDate.parse(SCANNER.nextLine().trim(), UI_DATE);
            } catch (DateTimeParseException e) {
                System.out.println("  ! Fecha inválida. Use el formato dd/mm/aaaa.");
            }
        }
    }

    public static String formatDate(LocalDate date) {
        return date.format(UI_DATE);
    }

    public static String formatCop(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(CO);
        format.setMaximumFractionDigits(0);
        return format.format(value) + " COP";
    }
}
