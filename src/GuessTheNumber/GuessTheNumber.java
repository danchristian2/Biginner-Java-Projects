import java.util.Scanner;
import java.util.Random;

public class GuessTheNumber {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        int target = random.nextInt(100) + 1; // 1 to 100
        int guess;
        int attempts = 0;

        System.out.println("I'm thinking of a number between 1 and 100!");

        do {
            System.out.print("Enter your guess: ");
            guess = scanner.nextInt();
            attempts++;

            if (guess < target) {
                System.out.println("Too low! Try again.");
            } else if (guess > target) {
                System.out.println("Too high! Try again.");
            } else {
                System.out.println("You got it in " + attempts + " attempts!");
            }
        } while (guess != target);

        scanner.close();
    }
}