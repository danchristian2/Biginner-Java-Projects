import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileOrganizer {

    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("Images", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp"));
        CATEGORIES.put("Documents", Arrays.asList("pdf", "doc", "docx", "txt", "rtf", "odt", "md"));
        CATEGORIES.put("Spreadsheets", Arrays.asList("xls", "xlsx", "csv", "ods"));
        CATEGORIES.put("Presentations", Arrays.asList("ppt", "pptx", "odp"));
        CATEGORIES.put("Audio", Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "m4a"));
        CATEGORIES.put("Video", Arrays.asList("mp4", "mov", "avi", "mkv", "wmv", "flv"));
        CATEGORIES.put("Archives", Arrays.asList("zip", "rar", "7z", "tar", "gz"));
        CATEGORIES.put("Code", Arrays.asList("java", "py", "js", "html", "css", "cpp", "c", "json", "xml"));
    }
    private static final String OTHER_CATEGORY = "Other";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java FileOrganizer <folder-path> [--dry-run]");
            return;
        }

        String folderPath = args[0];
        boolean dryRun = args.length > 1 && args[1].equalsIgnoreCase("--dry-run");

        Path targetDir = Paths.get(folderPath);

        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            System.out.println("Error: '" + folderPath + "' is not a valid directory.");
            return;
        }

        System.out.println((dryRun ? "[DRY RUN] " : "") + "Organizing files in: " + targetDir.toAbsolutePath());
        System.out.println("----------------------------------------------------");

        int movedCount = 0;
        int skippedCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    continue;
                }

                String fileName = entry.getFileName().toString();
                String extension = getExtension(fileName);
                String category = categorize(extension);

                Path categoryDir = targetDir.resolve(category);
                Path destination = categoryDir.resolve(fileName);

                try {
                    if (!dryRun) {
                        Files.createDirectories(categoryDir);
                        destination = resolveNameConflict(destination);
                        Files.move(entry, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                    System.out.println((dryRun ? "Would move: " : "Moved: ") + fileName + " -> " + category + "/");
                    movedCount++;
                } catch (IOException e) {
                    System.out.println("Skipped (error): " + fileName + " (" + e.getMessage() + ")");
                    skippedCount++;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading directory: " + e.getMessage());
            return;
        }

        System.out.println("----------------------------------------------------");
        System.out.println((dryRun ? "Would move " : "Moved ") + movedCount + " file(s). Skipped " + skippedCount + ".");
    }

    /** Extracts the lowercase file extension (without the dot), or "" if none. */
    private static String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    /** Determines which category a file extension belongs to. */
    private static String categorize(String extension) {
        for (Map.Entry<String, List<String>> entry : CATEGORIES.entrySet()) {
            if (entry.getValue().contains(extension)) {
                return entry.getKey();
            }
        }
        return OTHER_CATEGORY;
    }

    /** If the destination already exists, appends a number to avoid overwriting, e.g. photo(1).jpg */
    private static Path resolveNameConflict(Path destination) {
        if (!Files.exists(destination)) {
            return destination;
        }
        Path parent = destination.getParent();
        String fileName = destination.getFileName().toString();
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot != -1) {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot);
        }
        int counter = 1;
        Path candidate;
        do {
            candidate = parent.resolve(base + "(" + counter + ")" + ext);
            counter++;
        } while (Files.exists(candidate));
        return candidate;
    }
}