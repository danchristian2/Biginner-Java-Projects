import java.util.*;

import static java.awt.SystemColor.text;

public class BibleVerseUSSD {
    private static final Map<String, List<String>> VERSES = new LinkedHashMap<>();

    static {
        VERSES.put("Comfort", Arrays.asList(
                "Psalm 34:18|The LORD is close to the brokenhearted and saves those who are crushed in spirit.",
                "Matthew 11:28|Come to me, all you who are weary and burdened, and I will give you rest.",
                "2 Corinthians 1:3-4|The God of all comfort, who comforts us in all our troubles."
        ));
        VERSES.put("Strength", Arrays.asList(
                "Philippians 4:13|I can do all this through him who gives me strength.",
                "Isaiah 41:10|Fear not, for I am with you; be not dismayed, for I am your God.",
                "Joshua 1:9|Be strong and courageous. Do not be afraid; the LORD your God is with you."
        ));
        VERSES.put("Love", Arrays.asList(
                "John 3:16|For God so loved the world that he gave his one and only Son.",
                "1 John 4:19|We love because he first loved us.",
                "Romans 8:38-39|Nothing can separate us from the love of God in Christ Jesus."
        ));
        VERSES.put("Random Verse", null);
    }

    private static final List<String> ALL_VERSES = new ArrayList<>();
    static {
        for (List<String> list : VERSES.values()) {
            if (list != null) ALL_VERSES.addAll(list);
        }
    }

    private static final Random RANDOM = new Random();
    private static final String USSD_CODE = "*008#";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== USSD Simulator ===");
        System.out.println("Dial a code (try " + USSD_CODE + "), or 'exit' to quit.\n");

        while (true) {
            System.out.print("Dial: ");
            String dialed = scanner.nextLine().trim();

            if (dialed.equalsIgnoreCase("exit")) break;

            if (!dialed.equals(USSD_CODE)) {
                System.out.println("(Network) Invalid USSD code or number not recognized.\n");
            }

            runSession(scanner);
        System.out.println("Goodbye.");
    }
    private static void runSession(Scanner scanner) {
        String sessionText = ""; // accumulates like a real gateway's "text" param
        List<String> categories = new ArrayList<>(VERSES.keySet());

        while (true) {
            String response = handleRequest(sessionText, categories);

            if (response.startsWith("END")) {
                System.out.println(response.substring(4));
                System.out.println(); // blank line, session closed
                return;
            }

            // CON — show menu, wait for next input
            System.out.println(response.substring(4));
            System.out.print("Reply: ");
            String input = scanner.nextLine().trim();
            sessionText = sessionText.isEmpty() ? input : sessionText + "*" + input;
        }
    }

    private static String handleRequest(String text, List<String> categories) {
        if (text.isEmpty()) {
            StringBuilder sb = new StringBuilder("CON Welcome to Daily Bible Verse\n");
            for (int i = 0; i < categories.size(); i++) {
                sb.append(i + 1).append(". ").append(categories.get(i)).append("\n");
            }
            sb.append("0. Exit");
            return sb.toString();
        }

        String[] parts = text.split("\\*");
        String firstChoice = parts[0];

        if (firstChoice.equals("0")) {
            return "END Goodbye and God bless.";
        }

        int choice;
        try {
            choice = Integer.parseInt(firstChoice);
        } catch (NumberFormatException e) {
            return "END Invalid selection.";
        }

        if (choice < 1 || choice > categories.size()) {
            return "END Invalid selection.";
        }

        String category = categories.get(choice - 1);

        if (category.equals("Random Verse")) {
            String verse = ALL_VERSES.get(RANDOM.nextInt(ALL_VERSES.size()));
            return "END " + format(verse);
        }

        List<String> verses = VERSES.get(category);

        if (parts.length == 1) {
            // Show verses in this category
            StringBuilder sb = new StringBuilder("CON " + category + " verses\n");
            for (int i = 0; i < verses.size(); i++) {
                sb.append(i + 1).append(". ").append(verses.get(i).split("\\|")[0]).append("\n");
            }
            return sb.toString();
        } else {
            // parts[1] is verse selection
            int verseChoice;
            try {
                verseChoice = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return "END Invalid selection.";
            }
            if (verseChoice < 1 || verseChoice > verses.size()) {
                return "END Invalid selection.";
            }
            return "END " + format(verses.get(verseChoice - 1));
        }
    }

    private static String format(String entry) {
        String[] pieces = entry.split("\\|", 2);
        return pieces[0] + "\n" + pieces[1];
    }
}