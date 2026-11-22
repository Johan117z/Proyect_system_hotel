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

    /**
     * Lee un valor en pesos. Acepta "120000", "120.000", "95500,50", "120.000,50" y "95.5".
     * Rechaza los formatos ambiguos en lugar de adivinar.
     */
    public static BigDecimal readMoney(String message, boolean allowZero) {
        while (true) {
            System.out.print(message);
            BigDecimal value = parseMoney(SCANNER.nextLine());
            if (value == null) {
                System.out.println("  ! Valor inválido. Ejemplos: 120000, 120.000 o 95500,50.");
            } else if (value.signum() > 0 || (allowZero && value.signum() == 0)) {
                return value;
            } else {
                System.out.println(allowZero ? "  ! El valor no puede ser negativo." : "  ! El valor debe ser mayor que cero.");
            }
        }
    }

    /**
     * Convierte un texto en pesos a BigDecimal, o devuelve null si el formato no es válido.
     * <ul>
     *   <li>Punto seguido de exactamente 3 dígitos = separador de miles: "120.000" → 120000.</li>
     *   <li>Coma = decimales (formato colombiano): "120.000,50" → 120000.50.</li>
     *   <li>Punto seguido de 1 o 2 dígitos = decimales: "95.5" → 95.50.</li>
     * </ul>
     */
    static BigDecimal parseMoney(String text) {
        String input = text == null ? "" : text.trim().replace("$", "").replace(" ", "");
        String normalized;
        if (input.matches("\\d+") || input.matches("\\d+\\.\\d{1,2}")) {
            normalized = input;
        } else if (input.matches("\\d{1,3}(\\.\\d{3})+")) {
            normalized = input.replace(".", "");
        } else if (input.matches("\\d+,\\d{1,2}") || input.matches("\\d{1,3}(\\.\\d{3})+,\\d{1,2}")) {
            normalized = input.replace(".", "").replace(",", ".");
        } else {
            return null;
        }
        return new BigDecimal(normalized);
    }

    /** Pide confirmación (S/N) antes de una acción que no se puede deshacer. */
    public static boolean confirm(String message) {
        while (true) {
            System.out.print(message + " (S/N): ");
            String answer = SCANNER.nextLine().trim().toUpperCase();
            if (answer.equals("S") || answer.equals("SI") || answer.equals("SÍ")) {
                return true;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return false;
            }
            System.out.println("  ! Responda S o N.");
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

    /** Formato en pesos: sin decimales si el valor es entero ("$ 120.000 COP"), con centavos si los tiene ("$ 25.000,50 COP"). */
    public static String formatCop(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(CO);
        int decimals = value.stripTrailingZeros().scale() > 0 ? 2 : 0;
        format.setMinimumFractionDigits(decimals);
        format.setMaximumFractionDigits(decimals);
        return format.format(value) + " COP";
    }
}
