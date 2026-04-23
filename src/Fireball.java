public class Fireball {

    public int     x, y;
    public int     dirX, dirY;
    public boolean active = false;

    public void launch(int startX, int startY, int dx, int dy) {
        // TODO: aktif fireball başlat
    }

    public void move(Board board) {
        // TODO: her tick bir kare ilerle; duvara çarptıysa active = false
    }

    public boolean isActive() {
        return active;
    }
}
