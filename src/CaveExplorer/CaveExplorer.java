/*In this project you will see cool algorithms like DFS which helps alot in graph traversal*/
import java.util.*;
public class CaveExplorer {

    static final char WALL = '#';
    static final char FLOOR = '.';
    static final char PATH = '*';
    static final char START = 'S';
    static final char END = 'E';

    static final String RESET = "\u001B[0m";
    static final String GRAY = "\u001B[90m";
    static final String GREEN = "\u001B[32m";
    static final String YELLOW = "\u001B[93m";
    static final String CYAN = "\u001B[96m";
    static final String MAGENTA = "\u001B[95m";

    boolean[][] wall;
    int width, height;
    Random rng;

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.nanoTime();
        int width = args.length > 1 ? Integer.parseInt(args[1]) : 70;
        int height = args.length > 2 ? Integer.parseInt(args[2]) : 30;

        CaveExplorer cave = new CaveExplorer(width, height, seed);
        cave.generate(0.45, 5);
        cave.forceBorderWalls();

        List<int[]> largest = cave.largestOpenRegion();
        if (largest.size() < 2) {
            System.out.println("Cave collapsed in on itself. Try a different seed.");
            return;
        }

        int[] start = largest.get(0);
        int[] far1 = cave.farthestFrom(start, largest);
        int[] far2 = cave.farthestFrom(far1, largest);

        List<int[]> path = cave.bfsPath(far1, far2);

        System.out.println(MAGENTA + "=== CaveExplorer ===" + RESET);
        System.out.println("seed=" + seed + "  size=" + width + "x" + height
                + "  open cells=" + largest.size() + "  path length=" + (path.size() - 1));
        System.out.println();
        cave.render(far1, far2, path);
        System.out.println();
        System.out.println(GRAY + "#" + RESET + " wall   "
                + CYAN + "." + RESET + " open floor   "
                + YELLOW + "*" + RESET + " shortest path   "
                + GREEN + "S/E" + RESET + " entrance / exit");
    }

    CaveExplorer(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.rng = new Random(seed);
        this.wall = new boolean[height][width];
    }


    void generate(double fillProbability, int smoothingPasses) {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                wall[y][x] = rng.nextDouble() < fillProbability;

        for (int i = 0; i < smoothingPasses; i++) {
            smooth();
        }
    }

    void smooth() {
        boolean[][] next = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbors = countWallNeighbors(x, y);
                if (neighbors > 4) next[y][x] = true;
                else if (neighbors < 4) next[y][x] = false;
                else next[y][x] = wall[y][x];
            }
        }
        wall = next;
    }

    int countWallNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) count++; // treat out-of-bounds as wall
                else if (wall[ny][nx]) count++;
            }
        }
        return count;
    }

    void forceBorderWalls() {
        for (int x = 0; x < width; x++) {
            wall[0][x] = true;
            wall[height - 1][x] = true;
        }
        for (int y = 0; y < height; y++) {
            wall[y][0] = true;
            wall[y][width - 1] = true;
        }
    }

    List<int[]> largestOpenRegion() {
        boolean[][] visited = new boolean[height][width];
        List<int[]> best = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (wall[y][x] || visited[y][x]) continue;
                List<int[]> region = new ArrayList<>();
                Deque<int[]> stack = new ArrayDeque<>();
                stack.push(new int[]{x, y});
                visited[y][x] = true;
                while (!stack.isEmpty()) {
                    int[] cur = stack.pop();
                    region.add(cur);
                    for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                        int nx = cur[0] + d[0], ny = cur[1] + d[1];
                        if (nx >= 0 && ny >= 0 && nx < width && ny < height
                                && !wall[ny][nx] && !visited[ny][nx]) {
                            visited[ny][nx] = true;
                            stack.push(new int[]{nx, ny});
                        }
                    }
                }
                if (region.size() > best.size()) best = region;
            }
        }
        return best;
    }

    int[] farthestFrom(int[] from, List<int[]> region) {
        Set<Long> regionSet = new HashSet<>();
        for (int[] c : region) regionSet.add(key(c[0], c[1]));

        Map<Long, Integer> dist = new HashMap<>();
        Queue<int[]> q = new LinkedList<>();
        q.add(from);
        dist.put(key(from[0], from[1]), 0);

        int[] farthest = from;
        int maxDist = 0;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int d = dist.get(key(cur[0], cur[1]));
            if (d > maxDist) { maxDist = d; farthest = cur; }
            for (int[] dd : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nx = cur[0] + dd[0], ny = cur[1] + dd[1];
                long k = key(nx, ny);
                if (regionSet.contains(k) && !dist.containsKey(k)) {
                    dist.put(k, d + 1);
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return farthest;
    }

    List<int[]> bfsPath(int[] start, int[] end) {
        Map<Long, long[]> prev = new HashMap<>(); // key -> {parentKey, x, y}
        Queue<int[]> q = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        q.add(start);
        visited.add(key(start[0], start[1]));

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            if (cur[0] == end[0] && cur[1] == end[1]) break;
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nx = cur[0] + d[0], ny = cur[1] + d[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height || wall[ny][nx]) continue;
                long k = key(nx, ny);
                if (visited.contains(k)) continue;
                visited.add(k);
                prev.put(k, new long[]{key(cur[0], cur[1]), nx, ny});
                q.add(new int[]{nx, ny});
            }
        }

        LinkedList<int[]> path = new LinkedList<>();
        long cur = key(end[0], end[1]);
        path.addFirst(end);
        while (prev.containsKey(cur)) {
            long[] p = prev.get(cur);
            cur = p[0];
            int x = (int) (cur >> 32);
            int y = (int) (cur & 0xffffffffL);
            path.addFirst(new int[]{x, y});
        }
        return path;
    }

    long key(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    void render(int[] start, int[] end, List<int[]> path) {
        Set<Long> pathSet = new HashSet<>();
        for (int[] c : path) pathSet.add(key(c[0], c[1]));

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c;
                String color;
                if (x == start[0] && y == start[1]) { c = START; color = GREEN; }
                else if (x == end[0] && y == end[1]) { c = END; color = GREEN; }
                else if (wall[y][x]) { c = WALL; color = GRAY; }
                else if (pathSet.contains(key(x, y))) { c = PATH; color = YELLOW; }
                else { c = FLOOR; color = CYAN; }
                sb.append(color).append(c).append(RESET);
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}