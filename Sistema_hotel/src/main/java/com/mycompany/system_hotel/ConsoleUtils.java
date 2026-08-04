/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistema_hotel;

import java.util.Scanner;

public class ConsoleUtils {

    static final Scanner scanner = new Scanner(System.in);

    public static String readText(String message) {
        String input;
        while (true) {
            System.out.print(message);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.err.println("Field cannot be empty. Please try again.");
        }
    }

    public static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid integer number.");
            }
        }
    }

    public static double readDouble(String message) {
        while (true) {
            try {
                System.out.print(message);
                String input = scanner.nextLine().trim().replace(",", ".");
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.err.println("Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid numeric amount.");
            }
        }
    }
}