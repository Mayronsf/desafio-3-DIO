package br.com.dio.board.ui;

import java.util.Scanner;

// Leitura do teclado com validação: repete a pergunta até receber um valor válido
final class Input {

    private static final Scanner SCANNER = new Scanner(System.in);

    private Input() {
    }

    static String readText(String message) {
        while (true) {
            String text = readLine(message).trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("O valor não pode ficar em branco");
        }
    }

    static long readLong(String message) {
        while (true) {
            try {
                return Long.parseLong(readLine(message).trim());
            } catch (NumberFormatException e) {
                System.out.println("Informe um número válido");
            }
        }
    }

    static int readInt(String message, int min, int max) {
        while (true) {
            try {
                int number = Integer.parseInt(readLine(message).trim());
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Informe um número entre %d e %d%n", min, max);
        }
    }

    private static String readLine(String message) {
        System.out.print(message);
        if (!SCANNER.hasNextLine()) {
            System.exit(0);
        }
        return SCANNER.nextLine();
    }
}
