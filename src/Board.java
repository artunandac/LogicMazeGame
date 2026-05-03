import java.io.*;
import java.util.Random;

public class Board {
    public static final int ROWS = 21;
    public static final int COLS = 45;

    public char[][] grid;
    private Random rand = new Random();

    public Board() {
        grid = new char[ROWS][COLS];
    }

    public boolean loadMazeFromFile(String fileName) {
        try {
            java.io.InputStream is = Board.class.getResourceAsStream("/" + fileName);
            if (is == null) is = Board.class.getResourceAsStream(fileName);
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                for (int r = 0; r < ROWS; r++) {
                    String line = br.readLine();
                    if (line == null) { br.close(); return false; }
                    for (int c = 0; c < COLS; c++) {
                        grid[r][c] = (c < line.length()) ? line.charAt(c) : ' ';
                    }
                }
                br.close();
                return true;
            }
        } catch (IOException ignored) {}

        String[] paths = { fileName, "src/" + fileName, "../src/" + fileName };
        for (String path : paths) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(path));
                for (int r = 0; r < ROWS; r++) {
                    String line = br.readLine();
                    if (line == null) { br.close(); return false; }
                    for (int c = 0; c < COLS; c++) {
                        grid[r][c] = (c < line.length()) ? line.charAt(c) : ' ';
                    }
                }
                br.close();
                return true;
            } catch (IOException ignored) {}
        }
        return false;
    }

    public boolean checkConnected() {
        int startR = -1, startC = -1, total = 0;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] == ' ') {
                    total++;
                    if (startR == -1) { startR = r; startC = c; }
                }

        if (total == 0) return true;

        Queue bfsQueue = new Queue(ROWS * COLS);
        boolean[][] vis = new boolean[ROWS][COLS];
        bfsQueue.enqueue(startR * COLS + startC);
        vis[startR][startC] = true;
        int found = 1;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!bfsQueue.isEmpty()) {
            int encoded = (int) bfsQueue.dequeue();
            int cr = encoded / COLS;
            int cc = encoded % COLS;
            for (int d = 0; d < 4; d++) {
                int nr = cr + dr[d], nc = cc + dc[d];
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS
                        && !vis[nr][nc] && grid[nr][nc] == ' ') {
                    vis[nr][nc] = true;
                    bfsQueue.enqueue(nr * COLS + nc);
                    found++;
                }
            }
        }
        return found == total;
    }

    public boolean isWall(int col, int row) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return true;
        char ch = grid[row][col];
        return ch == '#';
    }

    public boolean isEmpty(int col, int row) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return false;
        return grid[row][col] == ' ';
    }

    public char getCell(int row, int col) { return grid[row][col]; }
    public void setCell(int row, int col, char ch) { grid[row][col] = ch; }
}
