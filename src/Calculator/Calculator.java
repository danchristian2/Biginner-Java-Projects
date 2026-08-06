import java.util.Scanner;

public class Calculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== Simple Java Calculator ===");
        System.out.println("Operations: + - * / %");
        System.out.println("Type 'exit' at any prompt to quit.\n");
        while (running) {
            try {
                Double num1 = readNumber(scanner, "Enter first number: ");
                if (num1 == null) break;

                String operator = readOperator(scanner);
                if (operator == null) break;

                Double num2 = readNumber(scanner, "Enter second number: ");
                if (num2 == null) break;

                double result = calculate(num1, num2, operator);
                System.out.printf("Result: %.2f %s %.2f = %.4f%n%n", num1, operator, num2, result);

            } catch (ArithmeticException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }

            System.out.print("Perform another calculation? (y/n): ");
            String again = scanner.nextLine().trim().toLowerCase();
            if (!again.equals("y") && !again.equals("yes")) {
                running = false;
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
        scanner.close();
    }
    private static Double readNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                return null;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, please try again (or type 'exit').");
            }
        }
    }
    private static String readOperator(Scanner scanner) {
        while (true) {
            System.out.print("Enter operator (+, -, *, /, %): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                return null;
            }
            if (input.equals("+") || input.equals("-") || input.equals("*")
                    || input.equals("/") || input.equals("%")) {
                return input;
            }
            System.out.println("Invalid operator, please try again (or type 'exit').");
        }
    }
    private static double calculate(double num1, double num2, String operator) {
        switch (operator) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                if (num2 == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                return num1 / num2;
            case "%":
                if (num2 == 0) {
                    throw new ArithmeticException("Cannot compute modulus with zero");
                }
                return num1 % num2;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}