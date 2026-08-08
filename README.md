# Beginner Java Projects

A collection of small Java projects I'm building while learning the language — starting simple and getting a bit more ambitious with each one. Feedback and pull requests are welcome!

## 📂 Projects

### 1. Bible Verse USSD Generator
A USSD application that delivers a Bible verse straight to your phone — no internet required. Dial the USSD code and get a random verse sent back as a text response.

- **Integration:** [Africa's Talking](https://africastalking.com/) USSD API
- **How it works:** the USSD gateway forwards the session to a Java backend, which picks a verse and returns it as the USSD response
- **Try it:** dial the configured USSD code on a supported network

### 2. CaveExplorer
A maze generator and pathfinder. It randomly generates a cave-like maze, then uses graph search to find the two points furthest apart from each other and draws the shortest path between them.

- **Maze generation:** randomized algorithm producing organic, cave-like layouts (rather than neat grid mazes)
- **Pathfinding:** graph search (BFS) to compute shortest paths and identify the maze's two farthest-apart points
- **Output:** a visual drawing of the maze with the shortest path highlighted

### 3. Web Scraper → CSV
A scraper that pulls data from web pages and exports it as CSV, ready to open in Excel/Sheets or feed into another program.

- **Input:** one or more URLs
- **Output:** a clean `.csv` file with the extracted data
- **Use case:** quick, lightweight data collection without needing a database

### 4. A Console Based Calculator
A simple yet powerful project to demonstarate the use of conditional loops.

## 🛠 Requirements

- Java 17+ (JDK)
- No external build tool required — each project can be compiled and run directly with `javac` / `java`

## 🚀 Running a Project

```bash
cd <project-folder>
javac Main.java
java Main
```

(See each project's own folder/README for specific run instructions and arguments.)

## 📌 Status

These are learning projects — code quality and structure will improve as I go. Suggestions on cleaner design, better error handling, or Java idioms I'm missing are very welcome.

## 📄 License

MIT — feel free to use, learn from, or build on any of this.
