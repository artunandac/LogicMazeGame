import java.util.Random;

public class Robot {
    public int x, y;
    public boolean targeted; // false=RANDOM(yeşil)  true=TARGETED(kırmızı)

    private int dirX = 1, dirY = 0;
    private static final Random rand = new Random();

    public Robot(int x, int y) {
        this.x = x;
        this.y = y;
        this.targeted = rand.nextBoolean();
        pickNewDirection();
    }

    // GameMain her 4 tickte çağırır
    public void step(Board board, Robot[] robots, int robotCount, int px, int py) {
        if (targeted)
            stepTargeted(board, robots, robotCount, px, py, px, py);
        else
            moveRandom(board, robots, robotCount, px, py);
    }

    // targetX/targetY: en yakın sembol yoksa player konumu geçilir
    public void stepTargeted(Board board, Robot[] robots, int robotCount,
                              int px, int py, int targetX, int targetY) {
        int[] dxs = {0, 0, -1, 1};
        int[] dys = {-1, 1, 0, 0};

        int bestDx = 0, bestDy = 0, bestDist = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            int nx = x + dxs[i];
            int ny = y + dys[i];
            if (board.isWall(nx, ny)) continue;
            if (isOccupied(nx, ny, robots, robotCount, px, py)) continue;
            int dist = Math.abs(nx - targetX) + Math.abs(ny - targetY);
            if (dist < bestDist) { bestDist = dist; bestDx = dxs[i]; bestDy = dys[i]; }
        }

        if (bestDist < Integer.MAX_VALUE) { x += bestDx; y += bestDy; }
    }

    private void moveRandom(Board board, Robot[] robots, int robotCount, int px, int py) {
        if (rand.nextInt(4) == 0) pickNewDirection();

        int nx = x + dirX, ny = y + dirY;

        if (board.isWall(nx, ny) || isOccupied(nx, ny, robots, robotCount, px, py)) {
            // duvara çarptı — yeni yön dene
            for (int attempt = 0; attempt < 8; attempt++) {
                pickNewDirection();
                nx = x + dirX; ny = y + dirY;
                if (!board.isWall(nx, ny) && !isOccupied(nx, ny, robots, robotCount, px, py))
                    break;
            }
        }

        nx = x + dirX; ny = y + dirY;
        if (!board.isWall(nx, ny) && !isOccupied(nx, ny, robots, robotCount, px, py)) {
            x = nx; y = ny;
        }
    }

    private void pickNewDirection() {
        int d = rand.nextInt(4);
        int[] dxs = {0, 0, -1, 1};
        int[] dys = {-1, 1, 0, 0};
        dirX = dxs[d]; dirY = dys[d];
    }

    private boolean isOccupied(int nx, int ny, Robot[] robots, int robotCount, int px, int py) {
        if (nx == px && ny == py) return true;
        for (int i = 0; i < robotCount; i++)
            if (robots[i] != this && robots[i].x == nx && robots[i].y == ny) return true;
        return false;
    }
}
