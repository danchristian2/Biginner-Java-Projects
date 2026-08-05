import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.regex.*;

public class NewsScraper {

    record Article(String url, String title, String author, String date, String body) {}

    public static void main(String[] args) throws Exception {
        List<String> urls = new ArrayList<>(Arrays.asList(args));
        if (urls.isEmpty()) {
            Path urlsFile = Path.of("urls.txt");
            if (Files.exists(urlsFile)) {
                for (String line : Files.readAllLines(urlsFile)) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) urls.add(line);
                }
            }
        }
        if (urls.isEmpty()) {
            System.out.println("Usage: java NewsScraper <url1> <url2> ...");
            System.out.println("   or: put one URL per line in urls.txt and run with no args");
            return;
        }

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        List<Article> articles = new ArrayList<>();
        for (String url : urls) {
            try {
                System.out.println("Fetching: " + url);
                String html = url.startsWith("file:")
                        ? Files.readString(Path.of(url.substring(5)))
                        : fetch(client, url);
                Article article = parse(url, html);
                articles.add(article);
                System.out.println("  -> \"" + article.title() + "\" | " + article.author()
                        + " | " + article.date() + " | " + article.body().length() + " chars of body text");
            } catch (Exception e) {
                System.out.println("  !! failed: " + e.getMessage());
            }
        }

        writeCsv(articles, "articles.csv");
        System.out.println("Saved " + articles.size() + " article(s) to articles.csv");
    }

    private static String fetch(HttpClient client, String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (compatible; SimpleNewsScraper/1.0)")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode());
        }
        return response.body();
    }


    private static final Pattern TITLE_TAG = Pattern.compile(
            "<title[^>]*>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_TAG = Pattern.compile(
            "<time[^>]*datetime=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_TAG = Pattern.compile(
            "<p[^>]*>(.*?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


    private static Pattern metaPattern(String key) {
        return Pattern.compile(
                "<meta[^>]*(?:(?:property|name)=[\"']" + Pattern.quote(key) + "[\"'][^>]*content=[\"'](.*?)[\"']"
                        + "|content=[\"'](.*?)[\"'][^>]*(?:property|name)=[\"']" + Pattern.quote(key) + "[\"'])[^>]*>",
                Pattern.CASE_INSENSITIVE);
    }

    static Article parse(String url, String html) {
        String title = firstGroup(metaPattern("og:title"), html);
        if (isBlank(title)) title = firstGroup(TITLE_TAG, html);
        title = cleanText(title);

        String author = firstGroup(metaPattern("article:author"), html);
        if (isBlank(author)) author = firstGroup(metaPattern("author"), html);
        author = cleanText(author);

        String date = firstGroup(metaPattern("article:published_time"), html);
        if (isBlank(date)) date = firstGroup(TIME_TAG, html);
        date = cleanText(date);

        String body = extractBody(html);

        return new Article(
                url,
                isBlank(title) ? "(unknown title)" : title,
                isBlank(author) ? "(unknown author)" : author,
                isBlank(date) ? "(unknown date)" : date,
                body);
    }

    private static String firstGroup(Pattern pattern, String html) {
        Matcher m = pattern.matcher(html);
        if (m.find()) {
            String g = m.group(1);
            return g != null ? g : m.group(2);
        }
        return null;
    }

    private static String extractBody(String html) {
        // drop script/style blocks so their text can't leak into the body
        String cleanedHtml = html.replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ");
        Matcher m = P_TAG.matcher(cleanedHtml);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String paragraph = cleanText(m.group(1));

            if (paragraph.length() > 40) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(paragraph);
            }
        }
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String cleanText(String raw) {
        if (raw == null) return "";
        String noTags = raw.replaceAll("(?is)<[^>]+>", " ");
        String decoded = decodeHtmlEntities(noTags);
        return decoded.replaceAll("\\s+", " ").trim();
    }

    private static String decodeHtmlEntities(String s) {
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&nbsp;", " ");
    }



    private static void writeCsv(List<Article> articles, String path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(path), StandardCharsets.UTF_8)) {
            writer.write("url,title,author,date,body");
            writer.newLine();
            for (Article a : articles) {
                writer.write(String.join(",",
                        csvEscape(a.url()),
                        csvEscape(a.title()),
                        csvEscape(a.author()),
                        csvEscape(a.date()),
                        csvEscape(a.body())));
                writer.newLine();
            }
        }
    }

    private static String csvEscape(String field) {
        if (field == null) field = "";
        boolean needsQuotes = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
        String escaped = field.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}