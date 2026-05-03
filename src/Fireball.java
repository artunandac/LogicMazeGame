public class Fireball {

    public int     x, y;
    public int     dirX, dirY;
    public boolean active = false;

    public void launch(int startX, int startY, int dx, int dy) {
        // yön belirtilmemişse default olarak sağa ateşle
        if (dx == 0 && dy == 0) {
            dx = 1;
            dy = 0;
        }

        this.dirX = dx;
        this.dirY = dy;

        this.x = startX + dx;
        this.y = startY + dy;
        this.active = true;
    }

    public void move(Board board) {
        if (!active) return;

        x += dirX;
        y += dirY;

        if (board.isWall(x, y)) {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }
}
