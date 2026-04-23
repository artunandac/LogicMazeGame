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

    // -------------------------------------------------------
    // maze.txt'yi buraya koy: proje kök klasörü (compile.bat ile aynı dizin)
    // Örnek: C:\Users\HP\OneDrive\Masaüstü\LogicMazeGame\maze.txt
    // -------------------------------------------------------
    public boolean loadMazeFromFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            for (int r = 0; r < ROWS; r++) {
                String line = br.readLine();
                if (line == null) { br.close(); return false; }
                for (int c = 0; c < COLS; c++) {
                    grid[r][c] = (c < line.length()) ? line.charAt(c) : ' ';
                }
            }
            br.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void generateMaze() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = ' ';
        addBorders();
        addWalls(35, 6);
    }

    private void addBorders() {
        for (int c = 0; c < COLS; c++) { grid[0][c] = '#'; grid[ROWS-1][c] = '#'; }
        for (int r = 0; r < ROWS; r++) { grid[r][0] = '#'; grid[r][COLS-1] = '#'; }
    }

    private void addWalls(int count, int maxLen) {
        for (int i = 0; i < count; i++) {
            int r = 1 + rand.nextInt(ROWS - 2);
            int c = 1 + rand.nextInt(COLS - 2);
            boolean horiz = rand.nextBoolean();
            int len = 2 + rand.nextInt(maxLen - 1);

            int[] savedR = new int[len];
            int[] savedC = new int[len];
            char[] backup = new char[len];
            boolean valid = true;
            int placed = 0;

            for (int j = 0; j < len; j++) {
                int nr = horiz ? r       : r + j;
                int nc = horiz ? c + j   : c;
                if (nr <= 0 || nr >= ROWS-1 || nc <= 0 || nc >= COLS-1) { valid = false; break; }
                savedR[j] = nr; savedC[j] = nc;
                backup[j] = grid[nr][nc];
                grid[nr][nc] = '#';
                placed = j + 1;
            }

            if (valid && checkConnected()) continue;

            for (int j = 0; j < placed; j++)
                grid[savedR[j]][savedC[j]] = backup[j];
        }
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

        int[] qr = new int[ROWS * COLS];
        int[] qc = new int[ROWS * COLS];
        boolean[][] vis = new boolean[ROWS][COLS];
        int head = 0, tail = 0;
        qr[tail] = startR; qc[tail++] = startC;
        vis[startR][startC] = true;
        int found = 1;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (head < tail) {
            int cr = qr[head], cc = qc[head++];
            for (int d = 0; d < 4; d++) {
                int nr = cr + dr[d], nc = cc + dc[d];
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS
                        && !vis[nr][nc] && grid[nr][nc] == ' ') {
                    vis[nr][nc] = true;
                    qr[tail] = nr; qc[tail++] = nc;
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
